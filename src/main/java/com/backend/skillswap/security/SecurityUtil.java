package com.backend.skillswap.security;

import com.backend.skillswap.entity.UserEntity;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static CustomUserDetails getCurrentUserDetails() {
        return (CustomUserDetails)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();
    }

    public static UserEntity getCurrentUser() {
        return getCurrentUserDetails().getUser();
    }
}


