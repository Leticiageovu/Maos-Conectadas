package com.maosconectadas.backend.product;

import com.maosconectadas.backend.auth.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> findAll(String search, UserAccount owner) {
        return search == null || search.isBlank()
            ? repository.findAllByOwnerId(owner.getId())
            : repository.findByOwnerIdAndNameContainingIgnoreCase(owner.getId(), search.trim());
    }

    public Product findById(Long id, UserAccount owner) {
        return repository.findByIdAndOwnerId(id, owner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    public Product create(ProductRequest request, UserAccount owner) {
        Product product = toEntity(request);
        product.setOwner(owner);
        return repository.save(product);
    }

    public Product update(Long id, ProductRequest request, UserAccount owner) {
        Product product = findById(id, owner);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        product.setImageUrl(request.imageUrl());
        product.setVariations(toVariations(request));
        return repository.save(product);
    }

    public void delete(Long id, UserAccount owner) {
        repository.delete(findById(id, owner));
    }

    private Product toEntity(ProductRequest request) {
        Product product = new Product(request.name(), request.description(), request.category(),
            request.price(), request.quantity(), request.imageUrl());
        product.setVariations(toVariations(request));
        return product;
    }

    private List<ProductVariation> toVariations(ProductRequest request) {
        if (request.variations() == null) return List.of();
        return request.variations().stream()
            .map(variation -> new ProductVariation(
                variation.size(), variation.color(), variation.quantity()))
            .collect(Collectors.toList());
    }
}
