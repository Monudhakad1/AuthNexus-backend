package com.authnexus.centralapplication.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AuthNexus Central auth application",
                description = "Its like a microservice that provides a login method ",
                contact = @Contact(
                        name = "Monu Dhakad",
                        url = "https://authnexus.com",
                        email = "monudhakad160@gmail.com"
                ),
                version = "1.0",
                summary = "AuthNexus Central auth application API documentation"
        ),
        security = {@SecurityRequirement(name = "bearerAuth"
        )}
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer", // auth header bearer
        bearerFormat = "JWT"
)
public class APIDocConfig {
}
