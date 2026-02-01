package com.backend.skillswap.config;

import com.backend.skillswap.security.CustomAccessDeniedHandler;
import com.backend.skillswap.security.CustomAuthenticationEntryPoint;
import com.backend.skillswap.security.JWT.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;   // Handles 401
    private final CustomAccessDeniedHandler accessDeniedHandler;             // Handles 403

    // Main Security Filter Chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Enable CORS with custom configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF (JWT is stateless)
                .csrf(csrf -> csrf.disable())

                // Stateless session (No HttpSession)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Custom authentication & authorization errors
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint) // 401 Unauthorized
                        .accessDeniedHandler(accessDeniedHandler)           // 403 Forbidden
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/api/profile/**").authenticated()

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // PUBLIC APIs (No Login Required)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/auth/logout").authenticated()  // Logout ke liye user already logged in hona chahiye, JWT token validate hona chahiye.

                        // User / Provider / Admin endpoints (Ye endpoints logged-in users ya providers ke liye)
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "PROVIDER", "ADMIN")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // JWT filter before Spring Security auth filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // JWT replaces username/password auth
        http
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        // AnonymousAuthentication ENABLE karo (403 ke liye REQUIRED)
        http.anonymous(anonymous -> {});
        return http.build();
    }

    // Used internally by Spring Security during authentication
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // CORS Configuration (Frontend Integration)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Allowed frontend origins
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allowed headers
        config.setAllowedHeaders(List.of("*"));

        // Allow cookies / authorization headers
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
