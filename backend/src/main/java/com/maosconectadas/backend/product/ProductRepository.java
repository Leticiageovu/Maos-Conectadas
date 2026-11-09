package com.maosconectadas.backend.product;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Override
    @EntityGraph(attributePaths = "variations")
    List<Product> findAll();

    @Override
    @EntityGraph(attributePaths = "variations")
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = "variations")
    List<Product> findByNameContainingIgnoreCase(String name);
}
