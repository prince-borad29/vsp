package com.vsp.authservice.service;

import com.vsp.authservice.dto.*;
import com.vsp.authservice.entity.Role;
import com.vsp.authservice.entity.UserAccount;
import com.vsp.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public MessageResponse registerUser(RegisterRequest request){
        // 1. Business Logic: Check if user exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken!");
        }

        // 2. Map DTO to Entity and Hash Password
        UserAccount user = new UserAccount();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);

        // 3. Database Operation
        userRepository.save(user);

        return new MessageResponse("User registered successfully");
    }

    public AuthResponse loginUser(LoginRequest request) {
        // 1. Database Operation
        Optional<UserAccount> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            UserAccount user = userOpt.get();

            // 2. Business Logic: Verify password
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {

                // 3. Generate Token
                String role = user.getRole().name();
                String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getName(), role);

                // 4. Map to DTOs
                UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(), role);

                return new AuthResponse(token, userDto);
            }
        }

        // 5. Business Logic: Handle failures securely (don't reveal if email vs password was wrong)
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

}
