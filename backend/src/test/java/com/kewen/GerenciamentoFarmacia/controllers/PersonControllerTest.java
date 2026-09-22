package com.kewen.GerenciamentoFarmacia.controllers;

import tools.jackson.databind.ObjectMapper;
import com.kewen.GerenciamentoFarmacia.entities.Person;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.PersonService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.context.annotation.Import(SecurityConfig.class)
@WebMvcTest(PersonController.class)
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1L);
        person.setFirstname("Carlos");
        person.setLastname("Oliveira");
        person.setCpf("11122233344");
    }

    // ======================== GET ========================

    @Test
    @DisplayName("GET /api/people - deve retornar lista de pessoas")
    void findAll_deveRetornarLista() throws Exception {
        when(personService.findAll()).thenReturn(List.of(person));

        mockMvc.perform(get("/api/people").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstname", is("Carlos")));
    }

    @Test
    @DisplayName("GET /api/people/{id} - deve retornar pessoa por id")
    void findById_deveRetornarPessoa() throws Exception {
        when(personService.findById(1L)).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/people/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("Carlos")));
    }

    @Test
    @DisplayName("GET /api/people/{id} - deve retornar 404 quando não encontrada")
    void findById_deveRetornar404() throws Exception {
        when(personService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/people/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/people/search/cpf - deve buscar por CPF")
    void findByCpf_deveBuscarPorCpf() throws Exception {
        when(personService.findByCpf("11122233344")).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/people/search/cpf").with(user("admin").roles("ADMIN")).param("cpf", "11122233344"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf", is("11122233344")));
    }

    @Test
    @DisplayName("GET /api/people/search/cpf - deve retornar 404 quando CPF não encontrado")
    void findByCpf_deveRetornar404() throws Exception {
        when(personService.findByCpf("99999999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/people/search/cpf").with(user("admin").roles("ADMIN")).param("cpf", "99999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/people/search/exists/cpf - deve verificar se CPF existe")
    void existsByCpf_deveVerificar() throws Exception {
        when(personService.existsByCpf("11122233344")).thenReturn(true);

        mockMvc.perform(get("/api/people/search/exists/cpf").with(user("admin").roles("ADMIN")).param("cpf", "11122233344"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /api/people/search/exists/cpf - deve verificar que CPF não existe")
    void existsByCpf_deveRetornarFalse() throws Exception {
        when(personService.existsByCpf("99999999999")).thenReturn(false);

        mockMvc.perform(get("/api/people/search/exists/cpf").with(user("admin").roles("ADMIN")).param("cpf", "99999999999"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // ======================== POST ========================

    @Test
    @DisplayName("POST /api/people - deve criar pessoa com sucesso")
    void create_deveCriarPessoa() throws Exception {
        when(personService.save(any(Person.class))).thenReturn(person);

        mockMvc.perform(post("/api/people")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(person)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstname", is("Carlos")));
    }

    @Test
    @DisplayName("POST /api/people - deve retornar 400 para CPF duplicado")
    void create_deveRetornar400ParaCpfDuplicado() throws Exception {
        when(personService.save(any(Person.class)))
                .thenThrow(new IllegalArgumentException("Já existe uma pessoa com o CPF informado"));

        mockMvc.perform(post("/api/people")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(person)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ======================== PUT ========================

    @Test
    @DisplayName("PUT /api/people/{id} - deve atualizar pessoa com sucesso")
    void update_deveAtualizarPessoa() throws Exception {
        when(personService.update(eq(1L), any(Person.class))).thenReturn(person);

        mockMvc.perform(put("/api/people/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(person)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("Carlos")));
    }

    @Test
    @DisplayName("PUT /api/people/{id} - deve retornar 400 para validação inválida")
    void update_deveRetornar400() throws Exception {
        when(personService.update(eq(1L), any(Person.class)))
                .thenThrow(new IllegalArgumentException("O CPF deve ter exatamente 11 dígitos"));

        mockMvc.perform(put("/api/people/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(person)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("PUT /api/people/{id} - deve retornar 404 quando não encontrada")
    void update_deveRetornar404() throws Exception {
        when(personService.update(eq(99L), any(Person.class)))
                .thenThrow(new RuntimeException("Pessoa não encontrada"));

        mockMvc.perform(put("/api/people/99")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(person)))
                .andExpect(status().isNotFound());
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE /api/people/{id} - deve deletar pessoa com sucesso")
    void delete_deveDeletarPessoa() throws Exception {
        doNothing().when(personService).deleteById(1L);

        mockMvc.perform(delete("/api/people/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/people/{id} - deve retornar 409 quando possui vínculos")
    void delete_deveRetornar409() throws Exception {
        doThrow(new IllegalStateException("Pessoa não pode ser deletada pois possui vínculos ativos"))
                .when(personService).deleteById(1L);

        mockMvc.perform(delete("/api/people/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("DELETE /api/people/{id} - deve retornar 404 quando não encontrada")
    void delete_deveRetornar404() throws Exception {
        doThrow(new RuntimeException("Pessoa não encontrada")).when(personService).deleteById(99L);

        mockMvc.perform(delete("/api/people/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/people - deve retornar 403 sem autenticação")
    void findAll_deveRetornar403SemAuth() throws Exception {
        mockMvc.perform(get("/api/people"))
                .andExpect(status().isForbidden());
    }
}
