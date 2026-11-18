package com.company.project.auth.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.project.auth.auth.dto.AuthResponse;
import com.company.project.auth.config.JwtKeyConfig;
import com.company.project.auth.config.JwtProperties;
import com.company.project.auth.user.AppUser;
import com.company.project.auth.user.Role;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class TokenServiceTest {

    private final RSAKey key = JwtKeyConfig.generateRsaKey();
    private final JwtProperties properties = new JwtProperties("test-issuer", Duration.ofMinutes(30));

    @Test
    void issuesTokenThatVerifiesWithThePublicKeyAndCarriesUserClaims() throws Exception {
        Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        TokenService tokenService = new TokenService(new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key))), properties, clock);
        AppUser user = new AppUser("ada@example.com", "Ada Lovelace", "{noop}x", Role.USER);
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", id);

        AuthResponse response = tokenService.issueFor(user);

        Jwt jwt = NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey()).build().decode(response.accessToken());
        assertThat(jwt.getSubject()).isEqualTo(id.toString());
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("test-issuer");
        assertThat(jwt.getClaimAsString(TokenService.CLAIM_EMAIL)).isEqualTo("ada@example.com");
        assertThat(jwt.getClaimAsString(TokenService.CLAIM_NAME)).isEqualTo("Ada Lovelace");
        assertThat(jwt.getClaimAsStringList(TokenService.CLAIM_ROLES)).containsExactly("USER");
        assertThat(response.expiresIn()).isEqualTo(1800);
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }
}
