package com.company.project.product.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

/**
 * This service's OWN copy of the user shape it needs - no shared classes with auth-service.
 * Unknown fields are ignored ("tolerant reader"), so auth-service can add fields freely.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserProfile(UUID id, String email, String fullName, String role) {
}
