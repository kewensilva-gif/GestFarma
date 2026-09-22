package com.kewen.GerenciamentoFarmacia.controllers;

import tools.jackson.databind.ObjectMapper;
import com.kewen.GerenciamentoFarmacia.entities.Category;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.CategoryService;
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
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Medicamentos");
        category.setEnabled(true);
    }

    // ======================== GET ========================

    @Test
    @DisplayName("GET /api/categories - deve retornar lista de categorias")
    void findAll_deveRetornarListaDeCategorias() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/categories").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Medicamentos")));
    }

    @Test
    @DisplayName("GET /api/categories/{id} - deve retornar categoria por id")
    void findById_deveRetornarCategoria() throws Exception {
        when(categoryService.findById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/categories/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Medicamentos")));
    }

    @Test
    @DisplayName("GET /api/categories/{id} - deve retornar 404 quando não encontrada")
    void findById_deveRetornar404() throws Exception {
        when(categoryService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/categories/search/name - deve buscar por nome")
    void findByName_deveBuscarPorNome() throws Exception {
        when(categoryService.findByName("Medicamentos")).thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/categories/search/name").with(user("admin").roles("ADMIN")).param("name", "Medicamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Medicamentos")));
    }

    // ======================== POST ========================

    @Test
    @DisplayName("POST /api/categories - deve criar categoria com sucesso")
    void create_deveCriarCategoria() throws Exception {
        when(categoryService.save(any(Category.class))).thenReturn(category);

        mockMvc.perform(post("/api/categories")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Medicamentos")));
    }

    @Test
    @DisplayName("POST /api/categories - deve retornar 400 para nome duplicado")
    void create_deveRetornar400ParaNomeDuplicado() throws Exception {
        when(categoryService.save(any(Category.class)))
                .thenThrow(new IllegalArgumentException("Já existe uma categoria com o nome 'Medicamentos'"));

        mockMvc.perform(post("/api/categories")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/categories - deve retornar 403 para role sem permissão")
    void create_deveRetornar403ParaRoleSemPermissao() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .with(user("customer").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isForbidden());
    }

    // ======================== PUT ========================

    @Test
    @DisplayName("PUT /api/categories/{id} - deve atualizar categoria com sucesso")
    void update_deveAtualizarCategoria() throws Exception {
        when(categoryService.update(eq(1L), any(Category.class))).thenReturn(category);

        mockMvc.perform(put("/api/categories/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Medicamentos")));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} - deve retornar 400 para validação inválida")
    void update_deveRetornar400ParaValidacaoInvalida() throws Exception {
        when(categoryService.update(eq(1L), any(Category.class)))
                .thenThrow(new IllegalArgumentException("O nome da categoria é obrigatório"));

        mockMvc.perform(put("/api/categories/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("O nome da categoria é obrigatório")));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} - deve retornar 404 quando não encontrada")
    void update_deveRetornar404() throws Exception {
        when(categoryService.update(eq(99L), any(Category.class)))
                .thenThrow(new RuntimeException("Categoria não encontrada"));

        mockMvc.perform(put("/api/categories/99")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isNotFound());
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE /api/categories/{id} - deve deletar categoria com sucesso")
    void delete_deveDeletarCategoria() throws Exception {
        doNothing().when(categoryService).deleteById(1L);

        mockMvc.perform(delete("/api/categories/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - deve retornar 409 quando possui produtos")
    void delete_deveRetornar409QuandoPossuiProdutos() throws Exception {
        doThrow(new IllegalStateException("Categoria não pode ser desativada pois possui produtos ativos vinculados"))
                .when(categoryService).deleteById(1L);

        mockMvc.perform(delete("/api/categories/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - deve retornar 404 quando não encontrada")
    void delete_deveRetornar404() throws Exception {
        doThrow(new RuntimeException("Categoria não encontrada")).when(categoryService).deleteById(99L);

        mockMvc.perform(delete("/api/categories/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
}
