package com.maosconectadas.backend.sale;

import com.maosconectadas.backend.auth.AuthService;
import com.maosconectadas.backend.auth.RegisterRequest;
import com.maosconectadas.backend.auth.UserAccount;
import com.maosconectadas.backend.auth.UserAccountRepository;
import com.maosconectadas.backend.product.Product;
import com.maosconectadas.backend.product.ProductService;
import com.maosconectadas.backend.product.ProductVariation;
import com.maosconectadas.backend.product.ProductRequest;
import com.maosconectadas.backend.product.VariationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:maos-sale-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
class SaleServiceTest {
    @Autowired
    private SaleService saleService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserAccountRepository userRepository;

    private UserAccount owner;

    @BeforeEach
    void createOwner() {
        authService.register(new RegisterRequest(
            "Artesã Teste", "11999999999", "artesa@example.com",
            "senhaSegura123", "senhaSegura123"));
        owner = userRepository.findByEmailIgnoreCase("artesa@example.com").orElseThrow();
    }

    @Test
    void recordsSaleAndDecreasesSelectedVariationStock() throws Exception {
        Product product = productService.create(new ProductRequest(
            "Bolsa artesanal", "Bolsa feita à mão", "Acessórios",
            new BigDecimal("89.90"), 6, null,
            List.of(
                new VariationRequest("P", "Azul", 3),
                new VariationRequest("M", "Vermelha", 3))), owner);

        mockMvc.perform(post("/api/sales")
                .with(user(owner))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId":%d,"variationIndex":1,"quantity":2}
                    """.formatted(product.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.productName").value("Bolsa artesanal"))
            .andExpect(jsonPath("$.variationLabel").value("Tamanho M / Vermelha"))
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.unitPrice").value(89.90))
            .andExpect(jsonPath("$.totalPrice").value(179.80));

        Product updated = productService.findById(product.getId(), owner);
        assertThat(updated.getVariations())
            .extracting(ProductVariation::getQuantity)
            .containsExactly(3, 1);
        assertThat(updated.getQuantity()).isEqualTo(4);
        assertThat(saleService.findAll(owner)).hasSize(1);
    }

    @Test
    void rejectsSaleGreaterThanStockWithoutCreatingSale() throws Exception {
        Product product = productService.create(new ProductRequest(
            "Bolsa artesanal", null, "Acessórios",
            new BigDecimal("89.90"), 1, null, List.of()), owner);

        mockMvc.perform(post("/api/sales")
                .with(user(owner))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId":%d,"variationIndex":0,"quantity":2}
                    """.formatted(product.getId())))
            .andExpect(status().isConflict());

        assertThat(productService.findById(product.getId(), owner).getQuantity()).isEqualTo(1);
        assertThat(saleService.findAll(owner)).isEmpty();
    }

    @Test
    void keepsSalesPrivateToTheirOwner() throws Exception {
        Product product = productService.create(new ProductRequest(
            "Bolsa artesanal", null, "Acessórios",
            new BigDecimal("89.90"), 2, null, List.of()), owner);
        saleService.create(new SaleRequest(product.getId(), 0, 1), owner);

        authService.register(new RegisterRequest(
            "Outro usuário", "11888888888", "outro@example.com",
            "senhaSegura456", "senhaSegura456"));
        UserAccount otherOwner = userRepository.findByEmailIgnoreCase("outro@example.com").orElseThrow();

        mockMvc.perform(get("/api/sales").with(user(otherOwner)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(post("/api/sales")
                .with(user(otherOwner))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId":%d,"variationIndex":0,"quantity":1}
                    """.formatted(product.getId())))
            .andExpect(status().isNotFound());
    }
}
