package com.company.project.product.product.dto;

import com.company.project.product.client.UserProfile;
import java.math.BigDecimal;

/**
 * Dashboard data combining this service's database with auth-service.
 *
 * @param owner                  profile of the caller
 * @param profileFromAuthService false when auth-service was unavailable and the profile was
 *                               built from the token instead (graceful degradation)
 * @param myProductCount         products owned by the caller
 * @param myInventoryValue       sum of the caller's product prices
 * @param totalProducts          products in the whole catalogue
 */
public record ProductSummary(
        UserProfile owner,
        boolean profileFromAuthService,
        long myProductCount,
        BigDecimal myInventoryValue,
        long totalProducts) {
}
