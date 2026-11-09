package com.maosconectadas.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService implements UserDetailsService {
    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfile register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }

        UserAccount account = new UserAccount(
            request.name().trim(),
            request.phone().trim(),
            email,
            passwordEncoder.encode(request.password()));
        return UserProfile.from(repository.save(account));
    }

    public UserProfile profile(UserAccount account) {
        return UserProfile.from(account);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmailIgnoreCase(normalizeEmail(email))
            .orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada."));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
