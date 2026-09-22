package com.kewen.GerenciamentoFarmacia.controllers;

import tools.jackson.databind.ObjectMapper;
import com.kewen.GerenciamentoFarmacia.dto.auth.AuthRequest;
import com.kewen.GerenciamentoFarmacia.dto.auth.AuthResponse;
import com.kewen.GerenciamentoFarmacia.dto.auth.RegisterRequest;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.context.annotation.Import(SecurityConfig.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    // ======================== REGISTER ========================

    @Test
    @DisplayName("POST /api/auth/register - deve registrar usuário com sucesso")
    void register_deveRegistrarComSucesso() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "newuser@email.com", "password123");
        AuthResponse response = new AuthResponse("jwt-token-123", "newuser", "newuser@email.com");

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", is("jwt-token-123")))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.email", is("newuser@email.com")));
    }

    @Test
    @DisplayName("POST /api/auth/register - deve retornar 400 para username duplicado")
    void register_deveRetornar400ParaUsernameDuplicado() throws Exception {
        RegisterRequest request = new RegisterRequest("existing", "existing@email.com", "password123");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Username já está em uso"));

        mockMvc.perform(post("/api/auth/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - deve retornar 400 para email duplicado")
    void register_deveRetornar400ParaEmailDuplicado() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "existing@email.com", "password123");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Email já está em uso"));

        mockMvc.perform(post("/api/auth/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - deve retornar 400 para dados inválidos (Bean Validation)")
    void register_deveRetornar400ParaDadosInvalidos() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "invalid", "12");

        mockMvc.perform(post("/api/auth/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ======================== LOGIN ========================

    @Test
    @DisplayName("POST /api/auth/login - deve autenticar com sucesso")
    void login_deveAutenticarComSucesso() throws Exception {
        AuthRequest request = new AuthRequest("admin", "password123");
        AuthResponse response = new AuthResponse("jwt-token-456", "admin", "admin@farmacia.com");

        when(authenticationService.authenticate(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("jwt-token-456")))
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.email", is("admin@farmacia.com")));
    }

    @Test
    @DisplayName("POST /api/auth/login - deve retornar 401 para credenciais inválidas")
    void login_deveRetornar401ParaCredenciaisInvalidas() throws Exception {
        AuthRequest request = new AuthRequest("admin", "wrongpassword");

        when(authenticationService.authenticate(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Credenciais inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - deve retornar 400 para dados em branco (Bean Validation)")
    void login_deveRetornar400ParaDadosEmBranco() throws Exception {
        AuthRequest invalidRequest = new AuthRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - deve funcionar sem autenticação prévia (permitAll)")
    void login_deveFuncionarSemAutenticacao() throws Exception {
        AuthRequest request = new AuthRequest("user", "password");
        AuthResponse response = new AuthResponse("token", "user", "user@email.com");

        when(authenticationService.authenticate(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/register - deve funcionar sem autenticação prévia (permitAll)")
    void register_deveFuncionarSemAutenticacao() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "new@email.com", "password123");
        AuthResponse response = new AuthResponse("token", "newuser", "new@email.com");

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // ======================== LOGOUT ========================

    @Test
    @DisplayName("POST /api/auth/logout - deve retornar 200 OK")
    void logout_deveRetornarOk() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }
}
