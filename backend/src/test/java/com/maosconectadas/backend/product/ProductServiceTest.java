package com.maosconectadas.backend.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductServiceTest {
    @Autowired
    private ProductService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsAndFindsProduct() {
        Product product = service.create(new ProductRequest(
            "Bolsa artesanal", "Bolsa feita à mão", "Acessórios",
            new BigDecimal("89.90"), 3, null, List.of()));

        assertThat(product.getId()).isNotNull();
        assertThat(service.findAll("bolsa"))
            .anyMatch(found -> found.getName().equals("Bolsa artesanal"));
    }

    @Test
    void returnsProductVariationsFromApi() throws Exception {
        Product product = service.create(new ProductRequest(
            "Bolsa artesanal", "Bolsa feita à mão", "Acessórios",
            new BigDecimal("89.90"), 3, null,
            List.of(new VariationRequest("Único", "Bege", 3))));

        mockMvc.perform(get("/api/products/{id}", product.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bolsa artesanal"))
            .andExpect(jsonPath("$.variations[0].color").value("Bege"))
            .andExpect(jsonPath("$.variations[0].quantity").value(3));
    }
}
