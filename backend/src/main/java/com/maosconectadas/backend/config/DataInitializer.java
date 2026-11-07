package com.maosconectadas.backend.config;

import com.maosconectadas.backend.product.Product;
import com.maosconectadas.backend.product.ProductRepository;
import com.maosconectadas.backend.product.ProductVariation;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                Product bag = new Product(
                    "Bolsa artesanal", "Bolsa de crochê para uso diário",
                    "Crochê", new BigDecimal("65.00"), 5, null);
                bag.setVariations(List.of(
                    new ProductVariation("Único", "Bege", 3),
                    new ProductVariation("Único", "Marrom", 2)));
                repository.save(bag);
            }
        };
    }
}
