package com.maosconectadas.backend.order;

import com.maosconectadas.backend.auth.UserAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderResponse> findAll(@AuthenticationPrincipal UserAccount owner) {
        return service.findAll(owner);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
        @Valid @RequestBody OrderRequest request,
        @AuthenticationPrincipal UserAccount owner
    ) {
        return service.create(request, owner);
    }
}
