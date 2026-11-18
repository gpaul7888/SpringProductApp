package com.company.project.auth.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    /** Also called service-to-service by product-service (with the caller's token relayed). */
    @GetMapping("/me")
    @Operation(summary = "Profile of the authenticated user")
    UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getById(UUID.fromString(jwt.getSubject()));
    }
}
