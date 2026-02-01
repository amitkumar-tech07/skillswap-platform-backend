package com.backend.skillswap.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SkillSwap Rest API",
                version = "1.0",
                description = "SkillSwap is a professional skill exchange platform where users can offer and learn skills through secure bookings, role-based access, escrow-backed payments, email verification, and a robust authentication system.",
contact = @Contact(
                        name = "Amit kumar",
                        email = "amitkr9942@gmail.com",
                        url = "https://github.com/amitkumar-tech07"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "LOCAL SERVER"), // Local dev server
                @Server(url = "https://api.skillswap.com", description = "PRODUCTION SERVER") // Production server
        }
)
@SecurityScheme(
        name = "BearerAuth", // Security scheme name (used in controllers)
        type = SecuritySchemeType.HTTP, // HTTP authentication type
        scheme = "bearer", // Auth scheme
        bearerFormat = "JWT" // Format for JWT tokens
)
public class OpenApiConfig {

    // Constant for security scheme name (used in controllers)
    public static final String SECURITY_SCHEME_NAME = "BearerAuth";

    // OpenAPI bean configuration
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Apply JWT security globally for secured endpoints
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                // Define JWT Bearer scheme in OpenAPI components
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new io.swagger.v3.oas.models.security.SecurityScheme()
                                                .name(SECURITY_SCHEME_NAME) // Scheme name
                                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP) // HTTP type
                                                .scheme("bearer") // Bearer auth
                                                .bearerFormat("JWT") // JWT format
                                )
                );
    }
}
