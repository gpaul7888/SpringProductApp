package com.company.project.product.product;

import com.company.project.product.client.UserClient;
import com.company.project.product.client.UserProfile;
import com.company.project.product.product.dto.ProductSummary;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Explicit virtual-thread fan-out.
 *
 * <p>The summary needs one remote call (auth-service) and three database queries. They are
 * independent, so each runs on its own virtual thread and the response time is roughly the
 * slowest of them instead of their sum. Virtual threads are cheap enough to create per request,
 * so a fresh executor in try-with-resources is the idiomatic pattern (closing waits for all tasks).
 */
@Service
public class ProductSummaryService {

    private static final Logger log = LoggerFactory.getLogger(ProductSummaryService.class);

    private final ProductRepository productRepository;
    private final UserClient userClient;

    public ProductSummaryService(ProductRepository productRepository, UserClient userClient) {
        this.productRepository = productRepository;
        this.userClient = userClient;
    }

    public ProductSummary summarize(CurrentUser user) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<UserProfile> profile = CompletableFuture.supplyAsync(() -> fetchProfile(user), executor);
            CompletableFuture<Long> myCount = CompletableFuture.supplyAsync(() -> productRepository.countByOwnerId(user.id()), executor);
            CompletableFuture<BigDecimal> myValue = CompletableFuture.supplyAsync(() -> productRepository.sumPriceByOwnerId(user.id()), executor);
            CompletableFuture<Long> total = CompletableFuture.supplyAsync(productRepository::count, executor);

            UserProfile owner = profile.join();
            return new ProductSummary(
                    owner != null ? owner : fallbackProfile(user),
                    owner != null,
                    myCount.join(),
                    myValue.join(),
                    total.join());
        }
    }

    /** Service-to-service call. Returns null instead of failing the whole summary if auth-service is down. */
    private UserProfile fetchProfile(CurrentUser user) {
        try {
            return userClient.currentUser(user.bearerToken());
        } catch (RestClientException | IllegalStateException ex) {
            log.warn("auth-service unavailable, using token data for user {}: {}", user.id(), ex.getMessage());
            return null;
        }
    }

    private static UserProfile fallbackProfile(CurrentUser user) {
        return new UserProfile(user.id(), null, user.name(), user.admin() ? "ADMIN" : "USER");
    }
}
