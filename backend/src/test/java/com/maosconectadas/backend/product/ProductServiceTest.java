package com.maosconectadas.backend.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductServiceTest {
    @Autowired
    private ProductService service;

    @Test
    void createsAndFindsProduct() {
        Product product = service.create(new ProductRequest(
            "Bolsa artesanal", "Bolsa feita à mão", "Acessórios",
            new BigDecimal("89.90"), 3, null, List.of()));

        assertThat(product.getId()).isNotNull();
        assertThat(service.findAll("bolsa"))
            .anyMatch(found -> found.getName().equals("Bolsa artesanal"));
    }
}
