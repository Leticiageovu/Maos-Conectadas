package com.maosconectadas.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;

public record RegisterRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 30) String phone,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 8, max = 72) String password,
    @NotBlank String confirmPassword
) {
    @AssertTrue(message = "As senhas não conferem")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }

    @AssertTrue(message = "A senha excede o limite permitido.")
    public boolean isPasswordWithinLimit() {
        return password != null && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
