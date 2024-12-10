package com.store.microservices.api_gateway.enums;

import lombok.RequiredArgsConstructor;

/**
 * Enum representing user roles in the system.
 * Each role has a corresponding string representation used for role-based access control.
 */
@RequiredArgsConstructor
public enum UserRole {
    CUSTOMER("ROLE_customer"), // Role for customers
    ADMIN("ROLE_admin");       // Role for administrators

    private final String roleName;

    /**
     * Returns the string representation of the role.
     *
     * @return the role name as a string
     */
    @Override
    public String toString() {
        return roleName;
    }
}
