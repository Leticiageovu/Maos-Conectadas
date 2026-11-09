package com.maosconectadas.backend.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    @EntityGraph(attributePaths = {"client", "product"})
    List<CustomerOrder> findAllByOwnerIdOrderByExpectedDateAsc(Long ownerId);

    boolean existsByClientId(Long clientId);
}
