package com.store.microservices.api_gateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

/**
 * Configuration class defining routes for API Gateway.
 * Routes incoming requests to appropriate microservices and handles fallback logic for circuit breakers.
 */
@Configuration
public class Routes {

    // URLs for microservices are injected from application properties
    @Value("${product.service.url}")
    private String productServiceUrl;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    /**
     * Configures routes for Product Service.
     * Includes a circuit breaker for handling service unavailability.
     */
    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {
        return route("product_service")
                .route(RequestPredicates.path("/api/v1/products/**"), HandlerFunctions.http(productServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    /**
     * Configures routes for Product Service Swagger API documentation.
     * Includes path rewriting and circuit breaker functionality.
     */
    @Bean
    public RouterFunction<ServerResponse> productServiceSwaggerRoute() {
        return route("product_service_swagger/**")
                .route(RequestPredicates.path("/aggregate/product-service/v3/api-docs"), HandlerFunctions.http(productServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    /**
     * Configures routes for Order Service.
     * Includes a circuit breaker for handling service unavailability.
     */
    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return route("order_service")
                .route(RequestPredicates.path("/api/v1/orders/**"), HandlerFunctions.http(orderServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    /**
     * Configures routes for Order Service Swagger API documentation.
     * Includes path rewriting and circuit breaker functionality.
     */
    @Bean
    public RouterFunction<ServerResponse> orderServiceSwaggerRoute() {
        return route("order_service_swagger")
                .route(RequestPredicates.path("/aggregate/order-service/v3/api-docs"), HandlerFunctions.http(orderServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    /**
     * Configures routes for Inventory Service.
     * Includes a circuit breaker for handling service unavailability.
     */
    @Bean
    public RouterFunction<ServerResponse> inventoryService() {
        return route("inventory_service")
                .route(RequestPredicates.path("/api/v1/inventory/**"), HandlerFunctions.http(inventoryServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    /**
     * Configures routes for Inventory Service Swagger API documentation.
     * Includes path rewriting and circuit breaker functionality.
     */
    @Bean
    public RouterFunction<ServerResponse> inventoryServiceSwaggerRoute() {
        return route("inventory_service_swagger")
                .route(RequestPredicates.path("/aggregate/inventory-service/v3/api-docs"), HandlerFunctions.http(inventoryServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    /**
     * Configures a fallback route to handle requests when services are unavailable.
     * Responds with a 503 Service Unavailable status.
     */
    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .GET("/fallbackRoute", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service is not available"))
                .build();
    }

}
