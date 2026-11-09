package com.maosconectadas.backend.product;

import com.maosconectadas.backend.auth.UserAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<Product> findAll(
        @RequestParam(required = false) String search,
        @AuthenticationPrincipal UserAccount owner
    ) {
        return service.findAll(search, owner);
    }

    @GetMapping("/{id}")
    public Product findById(@PathVariable Long id, @AuthenticationPrincipal UserAccount owner) {
        return service.findById(id, owner);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(
        @Valid @RequestBody ProductRequest request,
        @AuthenticationPrincipal UserAccount owner
    ) {
        return service.create(request, owner);
    }

    @PutMapping("/{id}")
    public Product update(
        @PathVariable Long id,
        @Valid @RequestBody ProductRequest request,
        @AuthenticationPrincipal UserAccount owner
    ) {
        return service.update(id, request, owner);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserAccount owner) {
        service.delete(id, owner);
    }
}
