package com.warehouse_kyoei.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse API documentation")
                        .version("1.0.0")
                        .description("API document for Warehouse management system")
                        .contact(new Contact()
                                .name("Mile")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")
                        // Add server later staging/production:
                        // new Server().url("https://staging.warehouse.es.com").description("Staging")
                ));
//                .components(new Components()
//                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
//                                .name(SECURITY_SCHEME_NAME)
//                                .type(SecurityScheme.Type.HTTP)
//                                .scheme("bearer")
//                                .bearerFormat("JWT")))
//                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

}
