package com.backend.skillswap.security;

import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UserEntity user;

    public UserEntity getUser() {
        return user;
    }

    // Returns user role as GrantedAuthority with mandatory "ROLE_" prefix (Spring Security requirement)
    // Convert ALL roles → GrantedAuthority (ROLE_ prefix is mandatory)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // Example: [USER, PROVIDER] → [ROLE_USER, ROLE_PROVIDER]
        return user.getRoles()
                .stream()
                .map(Role::name)                      // USER
                .map(role -> "ROLE_" + role)          // ROLE_USER
                .map(SimpleGrantedAuthority::new)     // GrantedAuthority
                .collect(Collectors.toList());
    }

    // Returns encoded password stored in DB for authentication ( Encoded password from DB)
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Username for Spring Security = email (because JWT subject = email)
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // Account expiry check (false hota to login block hota), true = account never expires
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Account lock check, true = account not locked
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Credentials expiry check, true =  Password never expires
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Enables login only if email is verified → Professional security feature
    @Override
    public boolean isEnabled() {
        return user.isEmailVerified();   // LOGIN BLOCK IF EMAIL NOT VERIFIED (unverified users auto-blocked)
    }
}


