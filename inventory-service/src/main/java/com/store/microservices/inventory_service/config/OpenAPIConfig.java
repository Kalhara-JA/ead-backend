package com.store.microservices.inventory_service.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up OpenAPI (Swagger) documentation.
 * Provides metadata and external documentation links for the Inventory Service API.
 */
@Configuration
public class OpenAPIConfig {

    /**
     * Configures OpenAPI documentation for the Inventory Service API.
     *
     * @return an OpenAPI instance with metadata and external documentation details
     */
    @Bean
    public OpenAPI inventoryServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service API") // Title of the API
                        .description("Inventory Service API for managing Inventory") // Short description of the API
                        .version("v1.0.0") // Version of the API
                        .license(new License().name("Apache 2.0"))) // License information
                .externalDocs(new ExternalDocumentation()
                        .description("Inventory Service Wiki Documentation") // Link to external documentation
                        .url("https://inventory-service-dummy-url.com/docs"));
    }
}
