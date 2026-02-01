package com.backend.skillswap.security.JWT;

import com.backend.skillswap.security.CustomAuthenticationEntryPoint;
import com.backend.skillswap.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomUserDetailsService userDetailsService;

    // Runs for every request and validates JWT → if valid then set Authentication in SecurityContext
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

            String path = request.getServletPath();

            // PUBLIC ENDPOINTS → BYPASS JWT COMPLETELY  → No JWT validation needed for these endpoints (Public / Open APIs → JWT check not required)
        if (path.startsWith("/api/auth")
                || path.startsWith("/api/public")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")) {

            filterChain.doFilter(request, response);
            return;
        }

            // Read Authorization header → token must start with Bearer
            String authHeader = request.getHeader("Authorization");

            // NO TOKEN → LET SPRING SECURITY DECIDE (401 ONLY IF REQUIRED)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {  // No token → let Spring Security handle (401 via EntryPoint)
                filterChain.doFilter(request, response);
                return;
            }

            // Extract token removing "Bearer "
            String token = authHeader.substring(7);

            try {

                // Extract username (email) from token
                String username = jwtUtil.extractUsername(token);

                // Process only if SecurityContext empty and username exists (Only process if Authentication not already set)
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Load user details from DB
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Validate token expiry
                    if (jwtUtil.isTokenValid(token, userDetails) && userDetails.isEnabled()) {

                        // Create authentication object with userId as principal + authorities
                        UsernamePasswordAuthenticationToken authentication  =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,   // principal = UserDetails object
                                        null,
                                        userDetails.getAuthorities()
                                );

                        // Set authentication inside SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication );
                    }
                }

                // Continue request flow /  always continue chain
                filterChain.doFilter(request, response);

            }catch (Exception ex) {
                SecurityContextHolder.clearContext();

                authenticationEntryPoint.commence(
                        request,
                        response,
                        new BadCredentialsException("Invalid or expired JWT")
                );
            }
    }
}

