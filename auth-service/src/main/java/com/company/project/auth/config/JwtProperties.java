package com.company.project.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe, immutable binding of {@code app.jwt.*} properties.
 *
 * @param issuer         value written to the {@code iss} claim and checked when decoding
 * @param accessTokenTtl lifetime of an access token (e.g. {@code 1h})
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(@NotBlank String issuer, @NotNull Duration accessTokenTtl) {
}
