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

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.authorizeHttpRequests(authorize -> {
                    // Permit all requests for free resources
                    authorize.requestMatchers(SecurityRouteConfig.FREE_RESOURCE_URLS).permitAll();

                    // Secure GET routes
                    for (Map.Entry<String, String[]> entry : SecurityRouteConfig.SECURED_GET_ROUTES.entrySet()) {
                        authorize.requestMatchers(HttpMethod.GET, entry.getKey()).hasAnyAuthority(entry.getValue());
                    }

                    // Secure POST routes
                    for (Map.Entry<String, String[]> entry : SecurityRouteConfig.SECURED_POST_ROUTES.entrySet()) {
                        authorize.requestMatchers(HttpMethod.POST, entry.getKey()).hasAnyAuthority(entry.getValue());
                    }

                    // Secure PUT routes
                    for (Map.Entry<String, String[]> entry : SecurityRouteConfig.SECURED_PUT_ROUTES.entrySet()) {
                        authorize.requestMatchers(HttpMethod.PUT, entry.getKey()).hasAnyAuthority(entry.getValue());
                    }


                    // Secure DELETE routes
                    for (Map.Entry<String, String[]> entry : SecurityRouteConfig.SECURED_DELETE_ROUTES.entrySet()) {
                        authorize.requestMatchers(HttpMethod.DELETE, entry.getKey()).hasAnyAuthority(entry.getValue());
                    }

                    // All other requests require authentication
                    authorize.anyRequest().authenticated();
                })
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public JwtAuthenticationConverter customJwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        List<String> validIssuers = List.of(
                "http://localhost:8181/realms/store",
                "http://keycloak:8080/realms/store",
                "https://auth.example.com/realms/store"
        );

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Add custom validation for issuer
        jwtDecoder.setJwtValidator((jwt) -> {
            String issuer = jwt.getIssuer().toString();
            if (validIssuers.contains(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid issuer: " + issuer, null));
        });

        return jwtDecoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Allow all origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")); // Allowed HTTP methods
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
        configuration.setAllowCredentials(false); // Set to false if no credentials are needed
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply configuration to all endpoints
        return source;
    }
}
