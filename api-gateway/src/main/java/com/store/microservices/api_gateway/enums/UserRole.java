package com.store.microservices.api_gateway.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserRole {
    CUSTOMER("ROLE_customer"),
    ADMIN("ROLE_admin");

    private final String roleName;

    @Override
    public String toString() {
        return roleName;
    }
}
