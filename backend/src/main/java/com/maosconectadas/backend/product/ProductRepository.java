package com.maosconectadas.backend.product;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = "variations")
    List<Product> findAllByOwnerId(Long ownerId);

    @EntityGraph(attributePaths = "variations")
    Optional<Product> findByIdAndOwnerId(Long id, Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id = :id and product.owner.id = :ownerId")
    Optional<Product> findByIdAndOwnerIdForUpdate(@Param("id") Long id, @Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = "variations")
    List<Product> findByOwnerIdAndNameContainingIgnoreCase(Long ownerId, String name);
}
