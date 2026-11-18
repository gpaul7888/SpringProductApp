package com.company.project.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies access tokens locally using auth-service's PUBLIC keys (JWKS).
 * The key set is downloaded once and cached; an unknown key id triggers a refresh,
 * so a restarted auth-service (new key) is picked up automatically.
 */
@Configuration(proxyBeanMethods = false)
public class JwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder(AuthServiceProperties properties, RestTemplate loadBalancedRestTemplate) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri())
                .restOperations(loadBalancedRestTemplate)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }
}
