package com.vsp.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("requiredRole");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 1. Check if the Authorization header exists
            if (!exchange.getRequest().getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

            // 2. Extract the token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            } else {
                return onError(exchange, "Invalid Authorization Header format", HttpStatus.UNAUTHORIZED);
            }

            // 3. Validate the token
            try {
                Key key = Keys.hmacShaKeyFor(secret.getBytes());
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(authHeader)
                        .getBody();

                String tokenRole = claims.get("role", String.class);
                String email = claims.getSubject();
                Object userId = claims.get("userId");

                // RBAC Check: If this route specifies a required role, verify it
                if (config.getRequiredRole() != null && !config.getRequiredRole().isBlank()) {
                    if (!config.getRequiredRole().equalsIgnoreCase(tokenRole)) {
                        return onError(exchange,"you are not authorized" ,HttpStatus.FORBIDDEN);
                    }
                }

                // Forward identity headers to the microservice
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Email", email)
                        .header("X-User-Role", tokenRole)
                        .header("X-User-Id", String.valueOf(userId))
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (Exception e) {
                return onError(exchange, "JWT Token is invalid or expired", HttpStatus.UNAUTHORIZED);
            }

        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        private String requiredRole;

        public String getRequiredRole() {
            return requiredRole;
        }

        public void setRequiredRole(String requiredRole) {
            this.requiredRole = requiredRole;
        }
    }
}