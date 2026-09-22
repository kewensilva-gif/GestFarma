package com.kewen.GerenciamentoFarmacia.controllers;

import tools.jackson.databind.ObjectMapper;
import com.kewen.GerenciamentoFarmacia.entities.Role;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.RoleService;
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
@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Role role;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.fromString("660e8400-e29b-41d4-a716-446655440000");

        role = new Role();
        role.setUuid(roleId);
        role.setName("ADMIN");
    }

    // ======================== GET ========================

    @Test
    @DisplayName("GET /api/roles - deve retornar lista de roles")
    void findAll_deveRetornarLista() throws Exception {
        when(roleService.findAll()).thenReturn(List.of(role));

        mockMvc.perform(get("/api/roles").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("ADMIN")));
    }

    @Test
    @DisplayName("GET /api/roles/{id} - deve retornar role por id")
    void findById_deveRetornarRole() throws Exception {
        when(roleService.findById(roleId)).thenReturn(Optional.of(role));

        mockMvc.perform(get("/api/roles/" + roleId).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("ADMIN")));
    }

    @Test
    @DisplayName("GET /api/roles/{id} - deve retornar 404 quando não encontrada")
    void findById_deveRetornar404() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(roleService.findById(unknownId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/roles/" + unknownId).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/roles/search/name - deve buscar por nome")
    void findByName_deveBuscar() throws Exception {
        when(roleService.findByName("ADMIN")).thenReturn(Optional.of(role));

        mockMvc.perform(get("/api/roles/search/name").with(user("admin").roles("ADMIN")).param("name", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("ADMIN")));
    }

    @Test
    @DisplayName("GET /api/roles/search/name - deve retornar 404 quando nome não encontrado")
    void findByName_deveRetornar404() throws Exception {
        when(roleService.findByName("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/roles/search/name").with(user("admin").roles("ADMIN")).param("name", "UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/roles/search/exists/name - deve verificar existência de nome")
    void existsByName_deveVerificar() throws Exception {
        when(roleService.existsByName("ADMIN")).thenReturn(true);

        mockMvc.perform(get("/api/roles/search/exists/name").with(user("admin").roles("ADMIN")).param("name", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /api/roles - deve retornar 403 para EMPLOYEE")
    void findAll_deveRetornar403() throws Exception {
        mockMvc.perform(get("/api/roles").with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    // ======================== POST ========================

    @Test
    @DisplayName("POST /api/roles - deve criar role com sucesso")
    void create_deveCriarRole() throws Exception {
        when(roleService.save(any(Role.class))).thenReturn(role);

        mockMvc.perform(post("/api/roles")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("ADMIN")));
    }

    @Test
    @DisplayName("POST /api/roles - deve retornar 400 para nome duplicado")
    void create_deveRetornar400() throws Exception {
        when(roleService.save(any(Role.class)))
                .thenThrow(new RuntimeException("Já existe uma role com o nome 'ADMIN'"));

        mockMvc.perform(post("/api/roles")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ======================== PUT ========================

    @Test
    @DisplayName("PUT /api/roles/{id} - deve atualizar role com sucesso")
    void update_deveAtualizarRole() throws Exception {
        when(roleService.update(eq(roleId), any(Role.class))).thenReturn(role);

        mockMvc.perform(put("/api/roles/" + roleId)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("ADMIN")));
    }

    @Test
    @DisplayName("PUT /api/roles/{id} - deve retornar 400 para validação inválida")
    void update_deveRetornar400() throws Exception {
        when(roleService.update(eq(roleId), any(Role.class)))
                .thenThrow(new IllegalArgumentException("O nome da role é obrigatório"));

        mockMvc.perform(put("/api/roles/" + roleId)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("PUT /api/roles/{id} - deve retornar 404 quando não encontrada")
    void update_deveRetornar404() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(roleService.update(eq(unknownId), any(Role.class)))
                .thenThrow(new RuntimeException("Role não encontrada"));

        mockMvc.perform(put("/api/roles/" + unknownId)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isNotFound());
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE /api/roles/{id} - deve deletar role com sucesso")
    void delete_deveDeletarRole() throws Exception {
        doNothing().when(roleService).deleteById(roleId);

        mockMvc.perform(delete("/api/roles/" + roleId).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/roles/{id} - deve retornar 409 quando possui usuários vinculados")
    void delete_deveRetornar409() throws Exception {
        doThrow(new IllegalStateException("Role não pode ser deletada pois possui usuários vinculados"))
                .when(roleService).deleteById(roleId);

        mockMvc.perform(delete("/api/roles/" + roleId).with(user("admin").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("DELETE /api/roles/{id} - deve retornar 404 quando não encontrada")
    void delete_deveRetornar404() throws Exception {
        UUID unknownId = UUID.randomUUID();
        doThrow(new RuntimeException("Role não encontrada")).when(roleService).deleteById(unknownId);

        mockMvc.perform(delete("/api/roles/" + unknownId).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/roles - deve retornar 403 sem autenticação")
    void findAll_deveRetornar403SemAuth() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isForbidden());
    }
}
