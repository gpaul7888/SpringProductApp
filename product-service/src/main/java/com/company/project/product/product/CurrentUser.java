package com.company.project.product.product;

import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * The caller, extracted from the verified JWT once in the controller so the service layer
 * does not depend on Spring Security types.
 *
 * @param id          user id ({@code sub} claim)
 * @param name        display name ({@code name} claim)
 * @param admin       whether the caller has the ADMIN role
 * @param bearerToken {@code "Bearer <jwt>"}, used to relay the caller's identity to auth-service
 */
public record CurrentUser(UUID id, String name, boolean admin, String bearerToken) {

    public static CurrentUser from(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new CurrentUser(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("name"),
                roles != null && roles.contains("ADMIN"),
                "Bearer " + jwt.getTokenValue());
    }

    @Override
    public String toString() {
        return "CurrentUser[id=" + id + ", name=" + name + ", admin=" + admin + "]";
    }
}
