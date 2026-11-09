package com.maosconectadas.backend.client;

import com.maosconectadas.backend.auth.UserAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping
    public List<Client> findAll(@AuthenticationPrincipal UserAccount owner) {
        return service.findAll(owner);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Client create(
        @Valid @RequestBody ClientRequest request,
        @AuthenticationPrincipal UserAccount owner
    ) {
        return service.create(request, owner);
    }

    @PutMapping("/{id}")
    public Client update(
        @PathVariable Long id,
        @Valid @RequestBody ClientRequest request,
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
