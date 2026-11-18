package com.company.project.product.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.project.product.product.dto.ProductRequest;
import com.company.project.product.product.dto.ProductResponse;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final CurrentUser ALICE = new CurrentUser(UUID.randomUUID(), "Alice", false, "Bearer a");
    private static final CurrentUser BOB = new CurrentUser(UUID.randomUUID(), "Bob", false, "Bearer b");
    private static final CurrentUser ADMIN = new CurrentUser(UUID.randomUUID(), "Admin", true, "Bearer c");

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createAssignsTheCallerAsOwner() {
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(new ProductRequest("  Pen ", "  ", new BigDecimal("2.50")), ALICE);

        assertThat(response.name()).isEqualTo("Pen");
        assertThat(response.description()).isNull();
        assertThat(response.ownerId()).isEqualTo(ALICE.id());
        assertThat(response.ownerName()).isEqualTo("Alice");
        assertThat(response.editable()).isTrue();
    }

    @Test
    void anotherUserCannotDeleteSomeoneElsesProduct() {
        Product alicesPen = new Product("Pen", null, BigDecimal.ONE, ALICE.id(), "Alice");
        when(productRepository.findById(1L)).thenReturn(Optional.of(alicesPen));

        assertThatThrownBy(() -> productService.delete(1L, BOB)).isInstanceOf(ProductAccessDeniedException.class);
        verify(productRepository, never()).delete(any());
    }

    @Test
    void adminCanDeleteAnyProduct() {
        Product alicesPen = new Product("Pen", null, BigDecimal.ONE, ALICE.id(), "Alice");
        when(productRepository.findById(1L)).thenReturn(Optional.of(alicesPen));

        productService.delete(1L, ADMIN);

        verify(productRepository).delete(alicesPen);
    }

    @Test
    void legacyProductsWithoutOwnerAreEditableByAnyUser() {
        Product legacy = new Product("Old", null, BigDecimal.TEN, null, null);
        when(productRepository.findById(2L)).thenReturn(Optional.of(legacy));
        when(productRepository.saveAndFlush(legacy)).thenReturn(legacy);

        ProductResponse response = productService.update(2L, new ProductRequest("Renamed", null, BigDecimal.TWO), BOB);

        assertThat(response.name()).isEqualTo("Renamed");
    }

    @Test
    void missingProductIsReportedAsNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.get(99L, ALICE)).isInstanceOf(ProductNotFoundException.class);
    }
}
