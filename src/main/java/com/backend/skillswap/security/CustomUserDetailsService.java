package com.backend.skillswap.security;

import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        // Fetch user by email OR username (case-insensitive), else throw exception
        UserEntity user = userRepository
                .findByEmailIgnoreCaseOrUsernameIgnoreCase(loginId, loginId)   // Hybrid login (email / username) &  JWT subject = email, so load the email is best
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Wrap entity into Spring Security compatible CustomUserDetails object
        return new CustomUserDetails(user);
    }
}

