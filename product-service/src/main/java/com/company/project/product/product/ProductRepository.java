package com.company.project.product.product;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    long countByOwnerId(UUID ownerId);

    @Query("select coalesce(sum(p.price), 0) from Product p where p.ownerId = :ownerId")
    BigDecimal sumPriceByOwnerId(@Param("ownerId") UUID ownerId);
}
