package com.bookworm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Worm E-Commerce Bookstore Platform API")
                        .version("1.0.0")
                        .description("Production Spring Boot 3 + Java 26 + MySQL REST API implementing full customer journey: " +
                                "Authentication, Order History & Reordering ('Buy In Again'), Recommendations, Catalog Search & Filtering, " +
                                "Tentative Delivery Estimation, Basket & Address Management, Gift Points Redemption, Payment Gateway Verification, and 48-Hour Order Cancellation.")
                        .contact(new Contact().name("Book Worm Engineering").email("dev@bookworm.example.com")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
