package com.store.microservices.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom JWT Authentication Converter to extract roles from JWT claims.
 * Maps roles from the "resource_access" claim to GrantedAuthority objects.
 */
public class CustomJwtAuthenticationConverter extends JwtAuthenticationConverter {

    @Value("${spring.security.oauth2.authorizationserver.client.client-id}")
    private String clientId;

    /**
     * Constructor sets up the custom authority extraction logic.
     */
    public CustomJwtAuthenticationConverter() {
        setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
    }

    /**
     * Extracts GrantedAuthority objects from JWT claims under "resource_access".
     * Ensures roles specific to the client ID are converted to authorities.
     *
     * @param jwt the JWT containing claims
     * @return a collection of GrantedAuthority objects
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            Object clientRolesObj = resourceAccess.get(clientId);
            if (clientRolesObj instanceof Map) {
                Map<String, Object> clientRoles = (Map<String, Object>) clientRolesObj;
                Object rolesObj = clientRoles.get("roles");
                if (rolesObj instanceof List) {
                    List<String> roles = (List<String>) rolesObj;
                    authorities.addAll(roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList()));
                }
            }
        }

        return authorities;
    }
}
