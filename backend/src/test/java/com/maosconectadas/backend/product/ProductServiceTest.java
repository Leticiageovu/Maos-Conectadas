package com.maosconectadas.backend.product;

import com.maosconectadas.backend.auth.AuthService;
import com.maosconectadas.backend.auth.RegisterRequest;
import com.maosconectadas.backend.auth.UserAccount;
import com.maosconectadas.backend.auth.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:maos-product-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
class ProductServiceTest {
    @Autowired
    private ProductService service;

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
    void createsAndFindsProductForOwner() {
        Product product = service.create(new ProductRequest(
            "Bolsa artesanal", "Bolsa feita à mão", "Acessórios",
            new BigDecimal("89.90"), 3, null, List.of()), owner);

        assertThat(product.getId()).isNotNull();
        assertThat(service.findAll("bolsa", owner))
            .anyMatch(found -> found.getName().equals("Bolsa artesanal"));
    }

    @Test
    void returnsProductVariationsFromApiOnlyToOwner() throws Exception {
        Product product = service.create(new ProductRequest(
            "Bolsa artesanal", "Bolsa feita à mão", "Acessórios",
            new BigDecimal("89.90"), 3, null,
            List.of(new VariationRequest("Único", "Bege", 3))), owner);
        authService.register(new RegisterRequest(
            "Outro usuário", "11888888888", "outro@example.com",
            "senhaSegura456", "senhaSegura456"));
        UserAccount otherOwner = userRepository.findByEmailIgnoreCase("outro@example.com").orElseThrow();

        mockMvc.perform(get("/api/products/{id}", product.getId()).with(user(owner)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bolsa artesanal"))
            .andExpect(jsonPath("$.variations[0].color").value("Bege"))
            .andExpect(jsonPath("$.variations[0].quantity").value(3));

        mockMvc.perform(get("/api/products/{id}", product.getId()).with(user(otherOwner)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deletesProductOnlyForItsOwner() throws Exception {
        Product product = service.create(new ProductRequest(
            "Bolsa artesanal", "Bolsa feita à mão", "Acessórios",
            new BigDecimal("89.90"), 3, null, List.of()), owner);
        authService.register(new RegisterRequest(
            "Outro usuário", "11888888888", "outro@example.com",
            "senhaSegura456", "senhaSegura456"));
        UserAccount otherOwner = userRepository.findByEmailIgnoreCase("outro@example.com").orElseThrow();

        mockMvc.perform(delete("/api/products/{id}", product.getId())
                .with(user(otherOwner))
                .with(csrf()))
            .andExpect(status().isNotFound());
        assertThat(service.findAll(null, owner)).hasSize(1);

        mockMvc.perform(delete("/api/products/{id}", product.getId())
                .with(user(owner))
                .with(csrf()))
            .andExpect(status().isNoContent());
        assertThat(service.findAll(null, owner)).isEmpty();
    }
}
