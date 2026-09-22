package com.kewen.GerenciamentoFarmacia.controllers;

import tools.jackson.databind.ObjectMapper;
import com.kewen.GerenciamentoFarmacia.dto.CustomerDto;
import com.kewen.GerenciamentoFarmacia.entities.Customer;
import com.kewen.GerenciamentoFarmacia.entities.Person;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.context.annotation.Import(SecurityConfig.class)
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Customer customer;
    private CustomerDto customerDto;

    @BeforeEach
    void setUp() {
        Person person = new Person();
        person.setId(1L);
        person.setFirstname("João");
        person.setLastname("Silva");
        person.setCpf("12345678901");

        customer = new Customer();
        customer.setId(1L);
        customer.setRegistrationDate(LocalDate.of(2024, 1, 15));
        customer.setPerson(person);

        customerDto = new CustomerDto(
            "João", "Silva", "12345678901",
            LocalDate.of(2024, 1, 15),
            "ROLE_CUSTOMER"
        );
    }

    // ======================== GET ========================

    @Test
    @DisplayName("GET /api/customers - deve retornar lista de clientes (ADMIN)")
    void findAll_deveRetornarListaComoAdmin() throws Exception {
        when(customerService.findAll()).thenReturn(List.of(customerDto));

        mockMvc.perform(get("/api/customers").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/customers - deve retornar lista de clientes (EMPLOYEE)")
    void findAll_deveRetornarListaComoEmployee() throws Exception {
        when(customerService.findAll()).thenReturn(List.of(customerDto));

        mockMvc.perform(get("/api/customers").with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/customers - deve retornar lista de clientes (CUSTOMER)")
    void findAll_deveRetornarListaComoCustomer() throws Exception {
        when(customerService.findAll()).thenReturn(List.of(customerDto));

        mockMvc.perform(get("/api/customers").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/customers/{id} - deve retornar cliente por id")
    void findById_deveRetornarCliente() throws Exception {
        when(customerService.findById(1L)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/customers/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/customers/{id} - deve retornar 404 quando não encontrado")
    void findById_deveRetornar404() throws Exception {
        when(customerService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/customers/search/after - deve buscar por data de registro após")
    void findByRegistrationAfter_deveBuscar() throws Exception {
        when(customerService.findByRegistrationAfter(LocalDate.of(2024, 1, 1))).thenReturn(List.of(customerDto));

        mockMvc.perform(get("/api/customers/search/after").with(user("admin").roles("ADMIN")).param("date", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/customers/search/before - deve buscar por data de registro antes")
    void findByRegistrationBefore_deveBuscar1() throws Exception {
        when(customerService.findByRegistrationBefore(LocalDate.of(2025, 1, 1))).thenReturn(List.of(customerDto));

        mockMvc.perform(get("/api/customers/search/before").with(user("admin").roles("ADMIN")).param("date", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ======================== POST ========================

    @Test
    @DisplayName("POST /api/customers - deve criar cliente com sucesso")
    void create_deveCriarCliente() throws Exception {
        when(customerService.save(any(Customer.class))).thenReturn(customer);

        mockMvc.perform(post("/api/customers")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("POST /api/customers - deve criar cliente com role EMPLOYEE")
    void create_deveCriarClienteComoEmployee() throws Exception {
        when(customerService.save(any(Customer.class))).thenReturn(customer);

        mockMvc.perform(post("/api/customers")
                        .with(user("employee").roles("EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/customers - deve retornar 400 para dados inválidos")
    void create_deveRetornar400() throws Exception {
        when(customerService.save(any(Customer.class)))
                .thenThrow(new IllegalArgumentException("A data de registro é obrigatória"));

        mockMvc.perform(post("/api/customers")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/customers - deve retornar 403 para CUSTOMER")
    void create_deveRetornar403() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .with(user("customer").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isForbidden());
    }

    // ======================== PUT ========================

    @Test
    @DisplayName("PUT /api/customers/{id} - deve atualizar cliente com sucesso")
    void update_deveAtualizarCliente() throws Exception {
        when(customerService.update(eq(1L), any(Customer.class))).thenReturn(customer);

        mockMvc.perform(put("/api/customers/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("PUT /api/customers/{id} - deve retornar 404 quando não encontrado")
    void update_deveRetornar404() throws Exception {
        when(customerService.update(eq(99L), any(Customer.class)))
                .thenThrow(new RuntimeException("Cliente não encontrado"));

        mockMvc.perform(put("/api/customers/99")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isNotFound());
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE /api/customers/{id} - deve deletar cliente com sucesso")
    void delete_deveDeletarCliente() throws Exception {
        doNothing().when(customerService).deleteById(1L);

        mockMvc.perform(delete("/api/customers/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - deve retornar 409 quando possui vendas")
    void delete_deveRetornar409() throws Exception {
        doThrow(new IllegalStateException("Cliente não pode ser deletado pois possui vendas vinculadas"))
                .when(customerService).deleteById(1L);

        mockMvc.perform(delete("/api/customers/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - deve retornar 404 quando não encontrado")
    void delete_deveRetornar404() throws Exception {
        doThrow(new RuntimeException("Cliente não encontrado")).when(customerService).deleteById(99L);

        mockMvc.perform(delete("/api/customers/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - deve retornar 403 para EMPLOYEE")
    void delete_deveRetornar403ParaEmployee() throws Exception {
        mockMvc.perform(delete("/api/customers/1").with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - deve retornar 403 sem autenticação")
    void delete_deveRetornar403() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isForbidden());
    }
}
