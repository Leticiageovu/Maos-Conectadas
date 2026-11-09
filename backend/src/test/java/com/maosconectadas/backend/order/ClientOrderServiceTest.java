package com.maosconectadas.backend.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maosconectadas.backend.auth.AuthService;
import com.maosconectadas.backend.auth.RegisterRequest;
import com.maosconectadas.backend.auth.UserAccount;
import com.maosconectadas.backend.auth.UserAccountRepository;
import com.maosconectadas.backend.client.Client;
import com.maosconectadas.backend.client.ClientRequest;
import com.maosconectadas.backend.client.ClientService;
import com.maosconectadas.backend.product.ProductRequest;
import com.maosconectadas.backend.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:maos-client-order-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
class ClientOrderServiceTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuthService authService;
    @Autowired private UserAccountRepository userRepository;
    @Autowired private ClientService clientService;
    @Autowired private OrderService orderService;
    @Autowired private ProductService productService;

    private UserAccount owner;

    @BeforeEach
    void createOwner() {
        authService.register(new RegisterRequest(
            "Artesã Teste", "11999999999", "artesa@example.com",
            "senhaSegura123", "senhaSegura123"));
        owner = userRepository.findByEmailIgnoreCase("artesa@example.com").orElseThrow();
    }

    @Test
    void createsClientAndOrderAndReturnsThemOnlyToOwner() throws Exception {
        var clientResult = mockMvc.perform(post("/api/clients")
                .with(user(owner))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new ClientRequest("Maria", "11988887777", "maria@example.com", "Prefere azul"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Maria"))
            .andReturn();
        long clientId = objectMapper.readTree(clientResult.getResponse().getContentAsString())
            .path("id").asLong();
        var product = productService.create(new ProductRequest(
            "Bolsa artesanal", "Crochê", "Acessórios",
            new BigDecimal("89.90"), 3, null, List.of()), owner);

        mockMvc.perform(post("/api/orders")
                .with(user(owner))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderRequest(
                    clientId, product.getId(), 2, LocalDate.now().plusDays(7), "Entrega"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.clientName").value("Maria"))
            .andExpect(jsonPath("$.productName").value("Bolsa artesanal"))
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.status").value("PENDENTE"));

        mockMvc.perform(get("/api/clients").with(user(owner)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Maria"))
            .andExpect(jsonPath("$[0].notes").value("Prefere azul"));

        mockMvc.perform(get("/api/orders").with(user(owner)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].clientName").value("Maria"))
            .andExpect(jsonPath("$[0].productName").value("Bolsa artesanal"))
            .andExpect(jsonPath("$[0].status").value("PENDENTE"));

        authService.register(new RegisterRequest(
            "Outra Pessoa", "11888888888", "outra@example.com",
            "senhaSegura456", "senhaSegura456"));
        UserAccount otherOwner = userRepository.findByEmailIgnoreCase("outra@example.com").orElseThrow();
        mockMvc.perform(get("/api/clients").with(user(otherOwner)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/orders").with(user(otherOwner)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(post("/api/orders")
                .with(user(otherOwner))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderRequest(
                    clientId, product.getId(), 1, LocalDate.now().plusDays(3), null))))
            .andExpect(status().isNotFound());
    }

    @Test
    void preventsDeletingClientWithOrderHistory() throws Exception {
        Client client = clientService.create(
            new ClientRequest("Maria", "11988887777", null, null), owner);
        var product = productService.create(new ProductRequest(
            "Bolsa artesanal", "Crochê", "Acessórios",
            new BigDecimal("89.90"), 3, null, List.of()), owner);
        orderService.create(new OrderRequest(
            client.getId(), product.getId(), 1, LocalDate.now().plusDays(7), null), owner);

        mockMvc.perform(delete("/api/clients/{id}", client.getId())
                .with(user(owner))
                .with(csrf()))
            .andExpect(status().isConflict());
    }
}
