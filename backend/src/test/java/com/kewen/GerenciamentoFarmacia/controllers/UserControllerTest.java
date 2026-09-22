package com.kewen.GerenciamentoFarmacia.controllers;

import tools.jackson.databind.ObjectMapper;
import com.kewen.GerenciamentoFarmacia.dto.UserDto;
import com.kewen.GerenciamentoFarmacia.entities.Role;
import com.kewen.GerenciamentoFarmacia.entities.User;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.context.annotation.Import(SecurityConfig.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User user;
    private UUID userId;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        Role role = new Role();
        role.setUuid(UUID.randomUUID());
        role.setName("ADMIN");

        user = new User();
        user.setUuid(userId);
        user.setUsername("admin");
        user.setEmail("admin@farmacia.com");
        user.setPassword("encodedPassword");
        user.setEnabled(true);
        user.setRole(role);

        userDto = new UserDto("admin", "admin@farmacia.com", true, "ADMIN");
    }

    // ======================== GET ========================

    @Test
    @DisplayName("GET /api/users - deve retornar lista de usuários")
    void findAll_deveRetornarLista() throws Exception {
        when(userService.findAll()).thenReturn(List.of(userDto));

        mockMvc.perform(get("/api/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("admin")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - deve retornar usuário por id")
    void findById_deveRetornarUsuario() throws Exception {
        when(userService.findById(userId)).thenReturn(Optional.of(userDto));

        mockMvc.perform(get("/api/users/" + userId).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - deve retornar 404 quando não encontrado")
    void findById_deveRetornar404() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(userService.findById(unknownId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/" + unknownId).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/search/username - deve buscar por username")
    void findByUsername_deveBuscar() throws Exception {
        when(userService.findByUsername("admin")).thenReturn(Optional.of(userDto));

        mockMvc.perform(get("/api/users/search/username").with(user("admin").roles("ADMIN")).param("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")));
    }

    @Test
    @DisplayName("GET /api/users/search/username - deve retornar 404 quando username não encontrado")
    void findByUsername_deveRetornar404() throws Exception {
        when(userService.findByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/search/username").with(user("admin").roles("ADMIN")).param("username", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/search/email - deve buscar por email")
    void findByEmail_deveBuscar() throws Exception {
        when(userService.findByEmail("admin@farmacia.com")).thenReturn(Optional.of(userDto));

        mockMvc.perform(get("/api/users/search/email").with(user("admin").roles("ADMIN")).param("email", "admin@farmacia.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("admin@farmacia.com")));
    }

    @Test
    @DisplayName("GET /api/users/enabled - deve retornar usuários habilitados")
    void findEnabled_deveRetornarHabilitados() throws Exception {
        when(userService.findEnabled()).thenReturn(List.of(userDto));

        mockMvc.perform(get("/api/users/enabled").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/users/disabled - deve retornar usuários desabilitados")
    void findDisabled_deveRetornarDesabilitados() throws Exception {
        when(userService.findDisabled()).thenReturn(List.of());

        mockMvc.perform(get("/api/users/disabled").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/users/search/exists/username - deve verificar existência de username")
    void existsByUsername_deveVerificar() throws Exception {
        when(userService.existsByUsername("admin")).thenReturn(true);

        mockMvc.perform(get("/api/users/search/exists/username").with(user("admin").roles("ADMIN")).param("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /api/users/search/exists/email - deve verificar existência de email")
    void existsByEmail_deveVerificar() throws Exception {
        when(userService.existsByEmail("admin@farmacia.com")).thenReturn(true);

        mockMvc.perform(get("/api/users/search/exists/email").with(user("admin").roles("ADMIN")).param("email", "admin@farmacia.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /api/users - deve retornar 403 para EMPLOYEE")
    void findAll_deveRetornar403() throws Exception {
        mockMvc.perform(get("/api/users").with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    // ======================== POST ========================

    @Test
    @DisplayName("POST /api/users - deve criar usuário com sucesso")
    void create_deveCriarUsuario() throws Exception {
        when(userService.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("admin")));
    }

    @Test
    @DisplayName("POST /api/users - deve retornar 400 para username duplicado")
    void create_deveRetornar400() throws Exception {
        when(userService.save(any(User.class)))
                .thenThrow(new IllegalArgumentException("Já existe um usuário com o username 'admin'"));

        mockMvc.perform(post("/api/users")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ======================== PUT ========================

    @Test
    @DisplayName("PUT /api/users/{id} - deve atualizar usuário com sucesso")
    void update_deveAtualizarUsuario() throws Exception {
        when(userService.update(eq(userId), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/" + userId)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - deve retornar 400 para validação inválida")
    void update_deveRetornar400() throws Exception {
        when(userService.update(eq(userId), any(User.class)))
                .thenThrow(new IllegalArgumentException("O email é obrigatório"));

        mockMvc.perform(put("/api/users/" + userId)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - deve retornar 404 quando não encontrado")
    void update_deveRetornar404() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(userService.update(eq(unknownId), any(User.class)))
                .thenThrow(new RuntimeException("Usuário não encontrado"));

        mockMvc.perform(put("/api/users/" + unknownId)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE /api/users/{id} - deve deletar usuário com sucesso")
    void delete_deveDeletarUsuario() throws Exception {
        when(userService.existsById(userId)).thenReturn(true);
        doNothing().when(userService).deleteById(userId);

        mockMvc.perform(delete("/api/users/" + userId).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - deve retornar 404 quando não encontrado")
    void delete_deveRetornar404() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(userService.existsById(unknownId)).thenReturn(false);

        mockMvc.perform(delete("/api/users/" + unknownId).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users - deve retornar 403 sem autenticação")
    void findAll_deveRetornar403SemAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }
}
