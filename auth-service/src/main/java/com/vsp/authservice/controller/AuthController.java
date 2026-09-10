package com.vsp.authservice.controller;

import com.vsp.authservice.dto.AuthResponse;
import com.vsp.authservice.dto.LoginRequest;
import com.vsp.authservice.dto.MessageResponse;
import com.vsp.authservice.dto.RegisterRequest;
import com.vsp.authservice.entity.UserAccount;
import com.vsp.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@RequestBody RegisterRequest request){

        MessageResponse response = userService.registerUser(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest request){
        AuthResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

}
