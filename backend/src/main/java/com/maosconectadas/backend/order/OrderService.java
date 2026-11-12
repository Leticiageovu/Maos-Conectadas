package com.maosconectadas.backend.order;

import com.maosconectadas.backend.auth.UserAccount;
import com.maosconectadas.backend.client.Client;
import com.maosconectadas.backend.client.ClientRepository;
import com.maosconectadas.backend.product.Product;
import com.maosconectadas.backend.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository repository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public OrderService(
        OrderRepository repository,
        ClientRepository clientRepository,
        ProductRepository productRepository
    ) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(UserAccount owner) {
        return repository.findAllByOwnerIdOrderByExpectedDateAsc(owner.getId())
            .stream()
            .map(OrderResponse::from)
            .toList();
    }

    public OrderResponse create(OrderRequest request, UserAccount owner) {
        Client client = clientRepository.findByIdAndOwnerId(request.clientId(), owner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrada"));
        Product product = productRepository.findByIdAndOwnerId(request.productId(), owner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        CustomerOrder order = repository.save(new CustomerOrder(
            client, product, request.quantity(), request.expectedDate(),
            clean(request.notes()), owner));
        return OrderResponse.from(order);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
