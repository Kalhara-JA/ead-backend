package com.store.microservices.api_gateway.config;

import com.store.microservices.api_gateway.enums.UserRole;
import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * Configuration class defining security rules for API routes.
 * Specifies free, partially secured, and fully secured routes with required HTTP methods and roles.
 */
public class SecurityRouteConfig {

    // Free or public routes accessible without authentication
    public static final String[] FREE_RESOURCE_URLS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/aggregate/**",
    };

    // Partially secured routes allowing specific HTTP methods without authentication
    public static final Map<String, HttpMethod[]> PARTIALLY_SECURED_ROUTES = Map.of(
            "/api/v1/products", new HttpMethod[]{HttpMethod.GET}
    );

    // Secured GET routes requiring specific roles
    public static final Map<String, String[]> SECURED_GET_ROUTES = Map.of(
            "/api/v1/inventory/warehouse/**", new String[]{UserRole.CUSTOMER.toString(), UserRole.ADMIN.toString()},
            "/api/v1/inventory/**", new String[]{UserRole.CUSTOMER.toString(), UserRole.ADMIN.toString()},
            "/api/v1/orders/**", new String[]{UserRole.CUSTOMER.toString(), UserRole.ADMIN.toString()},
            "/api/v1/products/**", new String[]{UserRole.CUSTOMER.toString(), UserRole.ADMIN.toString()}
    );

    // Secured POST routes requiring specific roles
    public static final Map<String, String[]> SECURED_POST_ROUTES = Map.of(
            "/api/v1/products/**", new String[]{UserRole.ADMIN.toString()},
            "/api/v1/inventory/warehouse/**", new String[]{UserRole.ADMIN.toString()},
            "/api/v1/inventory/**", new String[]{UserRole.ADMIN.toString()},
            "/api/v1/orders/**", new String[]{UserRole.CUSTOMER.toString(), UserRole.ADMIN.toString()}
    );

    // Secured PUT routes requiring specific roles
    public static final Map<String, String[]> SECURED_PUT_ROUTES = Map.of(
            "/api/v1/products/**", new String[]{UserRole.ADMIN.toString()},
            "/api/v1/inventory/warehouse/**", new String[]{UserRole.ADMIN.toString()},
            "/api/v1/orders/**", new String[]{UserRole.ADMIN.toString(), UserRole.CUSTOMER.toString()}
    );

    // Secured DELETE routes requiring specific roles
    public static final Map<String, String[]> SECURED_DELETE_ROUTES = Map.of(
            "/api/v1/products/**", new String[]{UserRole.ADMIN.toString()},
            "/api/v1/inventory/warehouse/**", new String[]{UserRole.ADMIN.toString()},
            "/api/v1/inventory/**", new String[]{UserRole.CUSTOMER.toString(), UserRole.ADMIN.toString()}
    );
}
