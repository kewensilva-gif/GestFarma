package com.kewen.GerenciamentoFarmacia.controllers;

import tools.jackson.databind.ObjectMapper;
import com.kewen.GerenciamentoFarmacia.dto.UserRegistrationRequest;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.UserRegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.context.annotation.Import(SecurityConfig.class)
@WebMvcTest(UserRegistrationController.class)
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserRegistrationService userRegistrationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UserRegistrationRequest createValidEmployeeRequest() {
        return new UserRegistrationRequest(
                "Maria",
                "Santos",
                "12345678901",
                "maria.santos",
                "maria@farmacia.com",
                "senha123",
                "EMPLOYEE",
                null,
                LocalDate.of(2024, 1, 15),
                new BigDecimal("3500.00")
        );
    }

    private UserRegistrationRequest createValidCustomerRequest() {
        return new UserRegistrationRequest(
                "João",
                "Silva",
                "98765432100",
                "joao.silva",
                "joao@email.com",
                "senha123",
                "CUSTOMER",
                LocalDate.of(2024, 6, 1),
                null,
                null
        );
    }

    // ======================== POST ========================

    @Test
    @DisplayName("POST /api/admin/register - deve registrar funcionário com sucesso")
    void register_deveRegistrarFuncionario() throws Exception {
        doNothing().when(userRegistrationService).adminRegisterUser(any(UserRegistrationRequest.class));

        mockMvc.perform(post("/api/admin/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidEmployeeRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.is("Usuário registrado com sucesso")));

        verify(userRegistrationService).adminRegisterUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/admin/register - deve registrar cliente com sucesso")
    void register_deveRegistrarCliente() throws Exception {
        doNothing().when(userRegistrationService).adminRegisterUser(any(UserRegistrationRequest.class));

        mockMvc.perform(post("/api/admin/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidCustomerRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/admin/register - deve retornar 400 para role inválida")
    void register_deveRetornar400ParaRoleInvalida() throws Exception {
        doThrow(new IllegalArgumentException("Role não encontrada: INVALID"))
                .when(userRegistrationService).adminRegisterUser(any(UserRegistrationRequest.class));

        mockMvc.perform(post("/api/admin/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidEmployeeRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/admin/register - deve retornar 400 para dados inválidos (Bean Validation)")
    void register_deveRetornar400ParaDadosInvalidos() throws Exception {
        // Request com campos obrigatórios em branco
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
                "", "", "", "", "invalid-email", "123", "INVALID_ROLE",
                null, null, null
        );

        mockMvc.perform(post("/api/admin/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/admin/register - deve retornar 400 para CPF duplicado")
    void register_deveRetornar400ParaCpfDuplicado() throws Exception {
        doThrow(new IllegalArgumentException("Já existe uma pessoa com o CPF informado"))
                .when(userRegistrationService).adminRegisterUser(any(UserRegistrationRequest.class));

        mockMvc.perform(post("/api/admin/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidEmployeeRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/admin/register - deve retornar 403 para EMPLOYEE")
    void register_deveRetornar403ParaEmployee() throws Exception {
        mockMvc.perform(post("/api/admin/register")
                        .with(user("employee").roles("EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidEmployeeRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/register - deve retornar 403 para CUSTOMER")
    void register_deveRetornar403ParaCustomer() throws Exception {
        mockMvc.perform(post("/api/admin/register")
                        .with(user("customer").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidCustomerRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/register - deve retornar 403 sem autenticação")
    void register_deveRetornar403SemAuth() throws Exception {
        mockMvc.perform(post("/api/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidEmployeeRequest())))
                .andExpect(status().isForbidden());
    }
}
