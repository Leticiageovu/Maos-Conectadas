package com.maosconectadas.backend.sale;

import com.maosconectadas.backend.auth.UserAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleService service;

    public SaleController(SaleService service) {
        this.service = service;
    }

    @GetMapping
    public List<SaleResponse> findAll(@AuthenticationPrincipal UserAccount owner) {
        return service.findAll(owner);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(
        @Valid @RequestBody SaleRequest request,
        @AuthenticationPrincipal UserAccount owner
    ) {
        return service.create(request, owner);
    }
}
