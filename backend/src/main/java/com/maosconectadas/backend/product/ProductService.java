package com.maosconectadas.backend.product;

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

    public List<Product> findAll(String search) {
        return search == null || search.isBlank()
            ? repository.findAll()
            : repository.findByNameContainingIgnoreCase(search.trim());
    }

    public Product findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    public Product create(ProductRequest request) {
        return repository.save(toEntity(request));
    }

    public Product update(Long id, ProductRequest request) {
        Product product = findById(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        product.setImageUrl(request.imageUrl());
        product.setVariations(toVariations(request));
        return repository.save(product);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
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
