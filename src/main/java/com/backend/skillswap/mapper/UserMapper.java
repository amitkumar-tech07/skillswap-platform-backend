package com.backend.skillswap.mapper;

import com.backend.skillswap.dto.request.RegisterRequest;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.enums.Role;

import java.util.List;

public class UserMapper {

    // DTO → ENTITY MAPPING
    public static UserEntity toEntity(RegisterRequest dto, List<Role> roles) {
        return UserEntity.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .roles(roles)
                .active(true)
                .emailVerified(false)
                .build();
    }


}
