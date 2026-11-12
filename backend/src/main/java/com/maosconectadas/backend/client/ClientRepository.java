package com.maosconectadas.backend.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findAllByOwnerIdOrderByNameAsc(Long ownerId);
    Optional<Client> findByIdAndOwnerId(Long id, Long ownerId);
}
