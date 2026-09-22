package com.kewen.GerenciamentoFarmacia.controllers;

import tools.jackson.databind.ObjectMapper;
import com.kewen.GerenciamentoFarmacia.dto.EmployeeDto;
import com.kewen.GerenciamentoFarmacia.entities.Employee;
import com.kewen.GerenciamentoFarmacia.entities.Person;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
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
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Employee employee;
    private EmployeeDto employeeDto;

    @BeforeEach
    void setUp() {
        Person person = new Person();
        person.setId(1L);
        person.setFirstname("Maria");
        person.setLastname("Santos");
        person.setCpf("98765432100");

        employee = new Employee();
        employee.setId(1L);
        employee.setHiringDate(LocalDate.of(2023, 6, 1));
        employee.setSalary(new BigDecimal("3500.00"));
        employee.setPerson(person);

        employeeDto = new EmployeeDto(
            "Maria", "Santos", "98765432100",
            LocalDate.of(2023, 6, 1),
            Optional.empty(),
            null,
            new BigDecimal("3500.00"),
            "ROLE_EMPLOYEE"
        );
    }

    // ======================== GET ========================

    @Test
    @DisplayName("GET /api/employees - deve retornar lista de funcionários")
    void findAll_deveRetornarLista() throws Exception {
        when(employeeService.findAll()).thenReturn(List.of(employeeDto));

        mockMvc.perform(get("/api/employees").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].salary", is(3500.00)));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - deve retornar funcionário por id")
    void findById_deveRetornarFuncionario() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/employees/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary", is(3500.00)));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - deve retornar 404 quando não encontrado")
    void findById_deveRetornar404() throws Exception {
        when(employeeService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/employees/search/after - deve buscar por data de contratação após")
    void findByHiringAfter_deveBuscar() throws Exception {
        when(employeeService.findByHiringAfter(LocalDate.of(2023, 1, 1))).thenReturn(List.of(employeeDto));

        mockMvc.perform(get("/api/employees/search/after").with(user("admin").roles("ADMIN")).param("date", "2023-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/employees/search/before - deve buscar por data de contratação antes")
    void findByHiringBefore_deveBuscar() throws Exception {
        when(employeeService.findByHiringBefore(LocalDate.of(2024, 1, 1))).thenReturn(List.of(employeeDto));

        mockMvc.perform(get("/api/employees/search/before").with(user("admin").roles("ADMIN")).param("date", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/employees/active - deve retornar funcionários ativos")
    void findActive_deveRetornarAtivos() throws Exception {
        when(employeeService.findActiveEmployees()).thenReturn(List.of(employeeDto));

        mockMvc.perform(get("/api/employees/active").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/employees/inactive - deve retornar funcionários inativos")
    void findInactive_deveRetornarInativos() throws Exception {
        when(employeeService.findInactiveEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees/inactive").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/employees - deve retornar 403 para EMPLOYEE")
    void findAll_deveRetornar403ParaEmployee() throws Exception {
        mockMvc.perform(get("/api/employees").with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    // ======================== POST ========================

    @Test
    @DisplayName("POST /api/employees - deve criar funcionário com sucesso")
    void create_deveCriarFuncionario() throws Exception {
        when(employeeService.save(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(post("/api/employees")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.salary", is(3500.00)));
    }

    @Test
    @DisplayName("POST /api/employees - deve retornar 400 para dados inválidos")
    void create_deveRetornar400() throws Exception {
        when(employeeService.save(any(Employee.class)))
                .thenThrow(new IllegalArgumentException("O salário deve ser maior que zero"));

        mockMvc.perform(post("/api/employees")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/employees - deve retornar 403 para role sem permissão")
    void create_deveRetornar403() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .with(user("employee").roles("EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isForbidden());
    }

    // ======================== PUT ========================

    @Test
    @DisplayName("PUT /api/employees/{id} - deve atualizar funcionário com sucesso")
    void update_deveAtualizarFuncionario() throws Exception {
        when(employeeService.update(eq(1L), any(Employee.class))).thenReturn(employee);

        mockMvc.perform(put("/api/employees/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary", is(3500.00)));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - deve retornar 400 para validação inválida")
    void update_deveRetornar400() throws Exception {
        when(employeeService.update(eq(1L), any(Employee.class)))
                .thenThrow(new IllegalArgumentException("O salário deve ser maior que zero"));

        mockMvc.perform(put("/api/employees/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - deve retornar 404 quando não encontrado")
    void update_deveRetornar404() throws Exception {
        when(employeeService.update(eq(99L), any(Employee.class)))
                .thenThrow(new RuntimeException("Funcionário não encontrado"));

        mockMvc.perform(put("/api/employees/99")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound());
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE /api/employees/{id} - deve deletar funcionário com sucesso")
    void delete_deveDeletarFuncionario() throws Exception {
        doNothing().when(employeeService).deleteById(1L);

        mockMvc.perform(delete("/api/employees/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - deve retornar 409 quando possui vendas")
    void delete_deveRetornar409() throws Exception {
        doThrow(new IllegalStateException("Funcionário não pode ser deletado pois possui vendas vinculadas"))
                .when(employeeService).deleteById(1L);

        mockMvc.perform(delete("/api/employees/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - deve retornar 404 quando não encontrado")
    void delete_deveRetornar404() throws Exception {
        doThrow(new RuntimeException("Funcionário não encontrado")).when(employeeService).deleteById(99L);

        mockMvc.perform(delete("/api/employees/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - deve retornar 403 sem autenticação")
    void delete_deveRetornar403() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }
}
