package com.company.project.product.product.dto;

import com.company.project.product.product.CurrentUser;
import com.company.project.product.product.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @param editable whether the caller may update/delete this product (lets the UI hide buttons)
 */
public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        UUID ownerId,
        String ownerName,
        Instant createdAt,
        Instant updatedAt,
        boolean editable) {

    public static ProductResponse from(Product product, CurrentUser viewer) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getOwnerId(),
                product.getOwnerName(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.canBeModifiedBy(viewer));
    }
}
