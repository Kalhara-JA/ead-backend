package com.store.microservices.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

/**
 * Security configuration for the API Gateway.
 * Implements JWT-based OAuth2 resource server security and custom CORS configuration.
 */
@Configuration
public class SecurityConfig {

    // URI for the JSON Web Key Set (JWKS) used for JWT verification
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * Defines the security filter chain.
     * - Configures route-specific access rules.
     * - Enables CORS and OAuth2 JWT authentication.
     *
     * @param httpSecurity the HttpSecurity instance
     * @return the configured SecurityFilterChain
     * @throws Exception if any configuration error occurs
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.authorizeHttpRequests(authorize -> {
                    // Permit access to public/free resources
                    authorize.requestMatchers(SecurityRouteConfig.FREE_RESOURCE_URLS).permitAll();

                    // Configure partially secured routes
                    SecurityRouteConfig.PARTIALLY_SECURED_ROUTES.forEach((route, methods) -> {
                        for (HttpMethod method : methods) {
                            authorize.requestMatchers(method, route).permitAll();
                        }
                    });

                    // Configure secured GET routes
                    for (Map.Entry<String, String[]> entry : SecurityRouteConfig.SECURED_GET_ROUTES.entrySet()) {
                        authorize.requestMatchers(HttpMethod.GET, entry.getKey()).hasAnyAuthority(entry.getValue());
                    }

                    // Configure secured POST routes
                    for (Map.Entry<String, String[]> entry : SecurityRouteConfig.SECURED_POST_ROUTES.entrySet()) {
                        authorize.requestMatchers(HttpMethod.POST, entry.getKey()).hasAnyAuthority(entry.getValue());
                    }

                    // Configure secured PUT routes
                    for (Map.Entry<String, String[]> entry : SecurityRouteConfig.SECURED_PUT_ROUTES.entrySet()) {
                        authorize.requestMatchers(HttpMethod.PUT, entry.getKey()).hasAnyAuthority(entry.getValue());
                    }

                    // Configure secured DELETE routes
                    for (Map.Entry<String, String[]> entry : SecurityRouteConfig.SECURED_DELETE_ROUTES.entrySet()) {
                        authorize.requestMatchers(HttpMethod.DELETE, entry.getKey()).hasAnyAuthority(entry.getValue());
                    }

                    // Deny all other requests
                    authorize.anyRequest().denyAll();
                })
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter())))
                .build();
    }

    /**
     * Custom JWT authentication converter to map JWT claims to granted authorities.
     *
     * @return the configured JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter customJwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter();
    }

    /**
     * Configures a JWT decoder with a custom validator for issuer verification.
     *
     * @return the configured JwtDecoder
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        List<String> validIssuers = List.of(
                "http://localhost:8181/realms/store",
                "http://keycloak:8080/realms/store",
                "https://auth.example.com/realms/store"
        );

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Custom validation for the issuer
        jwtDecoder.setJwtValidator((jwt) -> {
            String issuer = jwt.getIssuer().toString();
            if (validIssuers.contains(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid issuer: " + issuer, null));
        });

        return jwtDecoder;
    }

    /**
     * Configures CORS to allow requests from all origins with specific methods and headers.
     *
     * @return the configured CorsConfigurationSource
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Allow all origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")); // Allow common HTTP methods
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
        configuration.setAllowCredentials(false); // Credentials not required
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply CORS rules to all endpoints
        return source;
    }
}
