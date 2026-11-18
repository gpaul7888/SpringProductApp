package com.company.project.product.product;

public class ProductAccessDeniedException extends RuntimeException {

    public ProductAccessDeniedException(Long id) {
        super("You can only change products you own (product " + id + ")");
    }
}
