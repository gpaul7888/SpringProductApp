package com.company.project.product.client;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Contract for the parts of auth-service this service uses. Only an interface - the
 * implementation is generated in {@code HttpClientConfig}.
 *
 * <p>The caller's token is passed explicitly rather than read from the security context,
 * because the call may run on a different (virtual) thread that has no security context.
 */
@HttpExchange("/api/users")
public interface UserClient {

    @GetExchange("/me")
    UserProfile currentUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken);
}
