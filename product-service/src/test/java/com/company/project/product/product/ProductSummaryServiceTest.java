package com.company.project.product.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.company.project.product.client.UserClient;
import com.company.project.product.client.UserProfile;
import com.company.project.product.product.dto.ProductSummary;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class ProductSummaryServiceTest {

    private static final CurrentUser ALICE = new CurrentUser(UUID.randomUUID(), "Alice", false, "Bearer token");

    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserClient userClient;

    @InjectMocks
    private ProductSummaryService summaryService;

    @BeforeEach
    void stubRepository() {
        when(productRepository.countByOwnerId(ALICE.id())).thenReturn(3L);
        when(productRepository.sumPriceByOwnerId(ALICE.id())).thenReturn(new BigDecimal("42.50"));
        when(productRepository.count()).thenReturn(10L);
    }

    @Test
    void combinesTheRemoteProfileWithLocalStatistics() {
        UserProfile profile = new UserProfile(ALICE.id(), "alice@example.com", "Alice Liddell", "USER");
        when(userClient.currentUser("Bearer token")).thenReturn(profile);

        ProductSummary summary = summaryService.summarize(ALICE);

        assertThat(summary.owner()).isEqualTo(profile);
        assertThat(summary.profileFromAuthService()).isTrue();
        assertThat(summary.myProductCount()).isEqualTo(3);
        assertThat(summary.myInventoryValue()).isEqualByComparingTo("42.50");
        assertThat(summary.totalProducts()).isEqualTo(10);
    }

    @Test
    void degradesGracefullyWhenAuthServiceIsDown() {
        when(userClient.currentUser("Bearer token")).thenThrow(new ResourceAccessException("connection refused"));

        ProductSummary summary = summaryService.summarize(ALICE);

        assertThat(summary.profileFromAuthService()).isFalse();
        assertThat(summary.owner().fullName()).isEqualTo("Alice");
        assertThat(summary.myProductCount()).isEqualTo(3);
    }
}
