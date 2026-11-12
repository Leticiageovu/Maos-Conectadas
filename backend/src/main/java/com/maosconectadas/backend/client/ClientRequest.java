package com.maosconectadas.backend.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120)
    String name,

    @NotBlank(message = "Celular é obrigatório")
    @Size(max = 30)
    String phone,

    @Email(message = "E-mail inválido")
    @Size(max = 254)
    String email,

    @Size(max = 1000)
    String notes
) {}
