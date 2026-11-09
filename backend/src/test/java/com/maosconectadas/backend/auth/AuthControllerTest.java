package com.maosconectadas.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:maos-auth-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registersHashesPasswordAndLogsIn() throws Exception {
        RegisterRequest registration = new RegisterRequest(
            "Pessoa Artesã", "11987654321", "PESSOA@example.com",
            "senhaSegura123", "senhaSegura123");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registration)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Pessoa Artesã"))
            .andExpect(jsonPath("$.email").value("pessoa@example.com"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());

        UserAccount account = userRepository.findByEmailIgnoreCase("pessoa@example.com").orElseThrow();
        assertThat(account.getPassword()).isNotEqualTo(registration.password());
        assertThat(passwordEncoder.matches(registration.password(), account.getPassword())).isTrue();

        String loginJson = objectMapper.writeValueAsString(
            new LoginRequest(registration.email(), registration.password()));
        var loginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType("application/json")
                .content(loginJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Pessoa Artesã"))
            .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        mockMvc.perform(get("/api/auth/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Pessoa Artesã"));
    }

    @Test
    void rejectsProductRequestsWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidCredentialsAndMismatchedRegistrationPasswords() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                    new LoginRequest("missing@example.com", "senhaSegura123"))))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new RegisterRequest(
                    "Pessoa Artesã", "11987654321", "pessoa@example.com",
                    "senhaSegura123", "senhaDiferente456"))))
            .andExpect(status().isBadRequest());
    }
}
