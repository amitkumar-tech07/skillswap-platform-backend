package com.backend.skillswap.security;

import com.backend.skillswap.dto.common.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;


@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());


    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        // Safety Guard (most imp)
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(401)
                .errorCode("UNAUTHORIZED")
                .message("Authentication required")
                .path(request.getRequestURI())
                .build();

        // Write JSON safely
        objectMapper.writeValue(response.getOutputStream(), error);
    }

}

/* Note :  AUTHENTICATION ENTRY POINT (401)
Handles authentication failures BEFORE controller.
Triggers when:
- JWT is missing
- JWT is invalid
- JWT is expired
> Returns clean JSON instead of default HTML error.
 */
