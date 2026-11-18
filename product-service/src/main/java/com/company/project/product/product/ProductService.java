package com.company.project.product.product;

import com.company.project.product.product.dto.PageResponse;
import com.company.project.product.product.dto.ProductRequest;
import com.company.project.product.product.dto.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public PageResponse<ProductResponse> search(String query, Pageable pageable, CurrentUser viewer) {
        Page<Product> page = StringUtils.hasText(query)
                ? productRepository.findByNameContainingIgnoreCase(query.trim(), pageable)
                : productRepository.findAll(pageable);
        return PageResponse.from(page.map(product -> ProductResponse.from(product, viewer)));
    }

    public ProductResponse get(Long id, CurrentUser viewer) {
        return ProductResponse.from(findOrThrow(id), viewer);
    }

    @Transactional
    public ProductResponse create(ProductRequest request, CurrentUser owner) {
        Product product = new Product(request.name().trim(), trimToNull(request.description()), request.price(), owner.id(), owner.name());
        product = productRepository.saveAndFlush(product);
        log.info("User {} created product {}", owner.id(), product.getId());
        return ProductResponse.from(product, owner);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request, CurrentUser user) {
        Product product = findModifiableOrThrow(id, user);
        product.update(request.name().trim(), trimToNull(request.description()), request.price());
        // Flush now so the audit timestamp and version are in the response.
        return ProductResponse.from(productRepository.saveAndFlush(product), user);
    }

    @Transactional
    public void delete(Long id, CurrentUser user) {
        productRepository.delete(findModifiableOrThrow(id, user));
        log.info("User {} deleted product {}", user.id(), id);
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Product findModifiableOrThrow(Long id, CurrentUser user) {
        Product product = findOrThrow(id);
        if (!product.canBeModifiedBy(user)) {
            throw new ProductAccessDeniedException(id);
        }
        return product;
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
