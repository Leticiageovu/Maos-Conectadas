package com.maosconectadas.backend.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maosconectadas.backend.auth.AuthService;
import com.maosconectadas.backend.auth.RegisterRequest;
import com.maosconectadas.backend.auth.UserAccount;
import com.maosconectadas.backend.auth.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:maos-dashboard-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
class DashboardNotesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserAccountRepository userRepository;

    private UserAccount owner;

    @BeforeEach
    void createOwner() {
        authService.register(new RegisterRequest(
            "Artesã Teste", "11999999999", "artesa@example.com",
            "senhaSegura123", "senhaSegura123"));
        owner = userRepository.findByEmailIgnoreCase("artesa@example.com").orElseThrow();
    }

    @Test
    void savesAndLoadsDashboardNotesForOwner() throws Exception {
        mockMvc.perform(get("/api/dashboard/notes").with(user(owner)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notes").value(""));

        mockMvc.perform(put("/api/dashboard/notes")
                .with(user(owner))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DashboardNotesRequest("Ligar para cliente\nSeparar encomenda"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notes").value("Ligar para cliente\nSeparar encomenda"));

        mockMvc.perform(get("/api/dashboard/notes").with(user(owner)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notes").value("Ligar para cliente\nSeparar encomenda"));
    }

    @Test
    void doesNotExposeNotesToAnotherAccount() throws Exception {
        owner.setDashboardNotes("Lembrete particular");
        userRepository.save(owner);
        authService.register(new RegisterRequest(
            "Outro usuário", "11888888888", "outro@example.com",
            "senhaSegura456", "senhaSegura456"));
        UserAccount otherOwner = userRepository.findByEmailIgnoreCase("outro@example.com").orElseThrow();

        mockMvc.perform(get("/api/dashboard/notes").with(user(otherOwner)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notes").value(""));
    }

    @Test
    void rejectsNotesLongerThanFiveThousandCharacters() throws Exception {
        mockMvc.perform(put("/api/dashboard/notes")
                .with(user(owner))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DashboardNotesRequest("a".repeat(5001)))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deletesSavedNotesForOwner() throws Exception {
        owner.setDashboardNotes("Lembretes para excluir");
        userRepository.save(owner);

        mockMvc.perform(delete("/api/dashboard/notes")
                .with(user(owner))
                .with(csrf()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/dashboard/notes").with(user(owner)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notes").value(""));
    }
}
