package com.company.project.auth.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publishes the PUBLIC part of the signing key. Resource servers (product-service) download it
 * and verify tokens locally, so they never need a shared secret or a call per request.
 */
@RestController
@Tag(name = "Authentication")
class JwksController {

    private final Map<String, Object> publicJwkSet;

    JwksController(RSAKey rsaSigningKey) {
        this.publicJwkSet = new JWKSet(rsaSigningKey.toPublicJWK()).toJSONObject();
    }

    @GetMapping("/.well-known/jwks.json")
    @Operation(summary = "Public keys used to verify access tokens")
    Map<String, Object> jwks() {
        return publicJwkSet;
    }
}
