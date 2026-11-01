package com.vsp.authservice.controller;

import com.vsp.authservice.dto.*;
import com.vsp.authservice.service.JwtService;
import com.vsp.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@RequestBody RegisterRequest request) {

        MessageResponse response = userService.registerUser(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest request) {
        AuthResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-current-user")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.status(200).body(jwtService.verifyToken(token.replace("Bearer ", "")));
    }

}
