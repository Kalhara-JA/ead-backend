package com.store.microservices.api_gateway.config;

import com.store.microservices.api_gateway.enums.UserRole;

import java.util.Map;

public class SecurityRouteConfig {

    // Define free or public routes
    public static final String[] FREE_RESOURCE_URLS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/aggregate/**",
            "/api/v1/inventory/**"
    };

    // Define secured routes with their required roles
    public static final Map<String, String[]> SECURED_GET_ROUTES = Map.of(
            "/api/product/**", new String[]{UserRole.CUSTOMER.toString(), UserRole.ADMIN.toString()},
            "/api/v1/inventory/**", new String[]{UserRole.CUSTOMER.toString(), UserRole.ADMIN.toString()}
    );

    public static final Map<String, String[]> SECURED_POST_ROUTES = Map.of(
            "/api/product/**", new String[]{UserRole.ADMIN.toString()},
            "/api/v1/inventory/**", new String[]{ UserRole.ADMIN.toString()}

    );

    public static final Map<String, String[]> SECURED_PUT_ROUTES = Map.of(
            "/api/product/**", new String[]{UserRole.ADMIN.toString()}
    );

    public static final Map<String, String[]> SECURED_DELETE_ROUTES = Map.of(
            "/api/product/**", new String[]{UserRole.ADMIN.toString()}
    );
}
