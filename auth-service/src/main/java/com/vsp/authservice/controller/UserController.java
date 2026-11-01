package com.vsp.authservice.controller;

import com.vsp.authservice.dto.PasswordUpdateRequest;
import com.vsp.authservice.dto.UserUpdateRequest;
import com.vsp.authservice.dto.UserUpdateResponse;
import com.vsp.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/update-details")
    public ResponseEntity<UserUpdateResponse> updateUser(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok().body(userService.updateUser(request));
    }

    @PatchMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest request) {
        try {
            userService.updatePassword(request);
            return ResponseEntity.ok().body("Password updated successfully");
        } catch (RuntimeException e) {
           return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
