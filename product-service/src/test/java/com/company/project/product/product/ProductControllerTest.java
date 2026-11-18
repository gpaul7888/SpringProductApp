package com.company.project.product.product;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.project.product.config.SecurityConfig;
import com.company.project.product.product.dto.ProductResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Web-layer slice: real security filter chain, controller and error handling; services are mocked. */
@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private ProductSummaryService summaryService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void rejectsRequestsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    void returnsFieldErrorsAsProblemDetail() throws Exception {
        mockMvc.perform(post("/api/products").with(user())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "price": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        ProductResponse created = new ProductResponse(7L, "Pen", null, new BigDecimal("2.50"), USER_ID, "Ada",
                Instant.now(), Instant.now(), true);
        when(productService.create(any(), any())).thenReturn(created);

        mockMvc.perform(post("/api/products").with(user())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Pen", "price": 2.50}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/products/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.ownerName").value("Ada"));
    }

    private static RequestPostProcessor user() {
        return jwt().jwt(token -> token
                .subject(USER_ID.toString())
                .claim("name", "Ada")
                .claim("roles", List.of("USER")));
    }
}
