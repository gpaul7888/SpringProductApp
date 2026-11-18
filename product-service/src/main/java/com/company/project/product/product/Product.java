package com.company.project.product.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "product")
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Free text - a natural candidate for embeddings / semantic search (Spring AI) later. */
    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Id of the owning user in auth-service. Only an id - no foreign key across services. */
    @Column(name = "owner_id")
    private UUID ownerId;

    /** Denormalised copy of the owner's name so listing products needs no call to auth-service. */
    @Column(name = "owner_name", length = 100)
    private String ownerName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Optimistic locking: concurrent edits fail with 409 instead of silently overwriting. */
    @Version
    private Long version;

    protected Product() {
        // for JPA
    }

    public Product(String name, String description, BigDecimal price, UUID ownerId, String ownerName) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }

    public void update(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    /** Owners and admins may change a product; rows created before ownership existed are open to all users. */
    public boolean canBeModifiedBy(CurrentUser user) {
        return user.admin() || ownerId == null || ownerId.equals(user.id());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
