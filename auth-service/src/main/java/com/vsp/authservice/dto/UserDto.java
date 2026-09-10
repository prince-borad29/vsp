package com.vsp.authservice.dto;

import com.vsp.authservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String role;
}
