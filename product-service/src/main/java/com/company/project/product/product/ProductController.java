package com.company.project.product.product;

import com.company.project.product.product.dto.PageResponse;
import com.company.project.product.product.dto.ProductRequest;
import com.company.project.product.product.dto.ProductResponse;
import com.company.project.product.product.dto.ProductSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Thin controller: HTTP mapping and validation only; all rules live in the services. */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products")
class ProductController {

    private final ProductService productService;
    private final ProductSummaryService summaryService;

    ProductController(ProductService productService, ProductSummaryService summaryService) {
        this.productService = productService;
        this.summaryService = summaryService;
    }

    @GetMapping
    @Operation(summary = "List products, optionally filtered by name (paged: ?page=0&size=20&sort=price,desc)")
    PageResponse<ProductResponse> list(@RequestParam(name = "q", required = false) String query,
                                       @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
                                       @AuthenticationPrincipal Jwt jwt) {
        return productService.search(query, pageable, CurrentUser.from(jwt));
    }

    @GetMapping("/summary")
    @Operation(summary = "Dashboard numbers for the caller (calls auth-service; runs on virtual threads)")
    ProductSummary summary(@AuthenticationPrincipal Jwt jwt) {
        return summaryService.summarize(CurrentUser.from(jwt));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one product")
    ProductResponse get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return productService.get(id, CurrentUser.from(jwt));
    }

    @PostMapping
    @Operation(summary = "Create a product owned by the caller")
    ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request, @AuthenticationPrincipal Jwt jwt) {
        ProductResponse created = productService.create(request, CurrentUser.from(jwt));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product (owner or admin only)")
    ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request, @AuthenticationPrincipal Jwt jwt) {
        return productService.update(id, request, CurrentUser.from(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product (owner or admin only)")
    void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        productService.delete(id, CurrentUser.from(jwt));
    }
}
