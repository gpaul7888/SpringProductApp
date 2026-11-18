package com.company.project.product.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Body for create and update. There is deliberately no {@code id} or {@code owner} field:
 * the id comes from the URL / database and the owner from the verified token.
 * Limits mirror the database columns so bad input is a 400, not a database error.
 */
public record ProductRequest(
        @NotBlank(message = "Product name cannot be blank")
        @Size(max = 255, message = "Product name must be at most 255 characters")
        String name,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimals")
        BigDecimal price) {
}
