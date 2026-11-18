package com.company.project.product.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Everything product-service knows about auth-service. Note the URLs use the logical
 * service name ({@code http://auth-service}) - Eureka + the load balancer resolve the real address.
 *
 * @param baseUrl        base URL of auth-service's REST API
 * @param jwkSetUri      where to download the public keys that verify access tokens
 * @param issuer         expected {@code iss} claim
 * @param connectTimeout fail fast if auth-service is unreachable
 * @param readTimeout    upper bound for a single call
 */
@Validated
@ConfigurationProperties(prefix = "app.auth-service")
public record AuthServiceProperties(
        @NotBlank String baseUrl,
        @NotBlank String jwkSetUri,
        @NotBlank String issuer,
        @NotNull Duration connectTimeout,
        @NotNull Duration readTimeout) {
}
