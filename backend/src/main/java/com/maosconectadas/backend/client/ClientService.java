package com.maosconectadas.backend.client;

import com.maosconectadas.backend.auth.UserAccount;
import com.maosconectadas.backend.order.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ClientService {
    private final ClientRepository repository;
    private final OrderRepository orderRepository;

    public ClientService(ClientRepository repository, OrderRepository orderRepository) {
        this.repository = repository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<Client> findAll(UserAccount owner) {
        return repository.findAllByOwnerIdOrderByNameAsc(owner.getId());
    }

    public Client create(ClientRequest request, UserAccount owner) {
        return repository.save(new Client(
            request.name().trim(), request.phone().trim(), clean(request.email()),
            clean(request.notes()), owner));
    }

    public Client update(Long id, ClientRequest request, UserAccount owner) {
        Client client = findById(id, owner);
        client.setName(request.name().trim());
        client.setPhone(request.phone().trim());
        client.setEmail(clean(request.email()));
        client.setNotes(clean(request.notes()));
        return repository.save(client);
    }

    public void delete(Long id, UserAccount owner) {
        Client client = findById(id, owner);
        if (orderRepository.existsByClientId(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Não é possível excluir uma cliente que possui encomendas.");
        }
        repository.delete(client);
    }

    private Client findById(Long id, UserAccount owner) {
        return repository.findByIdAndOwnerId(id, owner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrada"));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
