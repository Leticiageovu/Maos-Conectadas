package com.maosconectadas.backend.product;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = "variations")
    List<Product> findAllByOwnerId(Long ownerId);

    @EntityGraph(attributePaths = "variations")
    Optional<Product> findByIdAndOwnerId(Long id, Long ownerId);

    @EntityGraph(attributePaths = "variations")
    List<Product> findByOwnerIdAndNameContainingIgnoreCase(Long ownerId, String name);
}
