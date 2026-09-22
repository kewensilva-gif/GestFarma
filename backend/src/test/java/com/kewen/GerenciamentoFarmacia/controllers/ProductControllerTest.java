package com.kewen.GerenciamentoFarmacia.controllers;

import tools.jackson.databind.ObjectMapper;
import com.kewen.GerenciamentoFarmacia.entities.Category;
import com.kewen.GerenciamentoFarmacia.entities.Product;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.ProductService;
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
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Medicamentos");
        category.setEnabled(true);

        product = new Product();
        product.setId(1L);
        product.setName("Paracetamol 500mg");
        product.setBarcode("7891234560001");
        product.setUnitPrice(new BigDecimal("12.50"));
        product.setStockQuantity(100);
        product.setExpirationDate(LocalDate.of(2027, 12, 31));
        product.setCategory(category);
        product.setEnabled(true);
    }

    // ======================== GET ========================

    @Test
    @DisplayName("GET /api/products - deve retornar lista de produtos")
    void findAll_deveRetornarListaDeProdutos() throws Exception {
        when(productService.findAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Paracetamol 500mg")));
    }

    @Test
    @DisplayName("GET /api/products/{id} - deve retornar produto por id")
    void findById_deveRetornarProduto() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Paracetamol 500mg")))
                .andExpect(jsonPath("$.barcode", is("7891234560001")));
    }

    @Test
    @DisplayName("GET /api/products/{id} - deve retornar 404 quando não encontrado")
    void findById_deveRetornar404() throws Exception {
        when(productService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/products/search/barcode - deve buscar por código de barras")
    void findByBarcode_deveBuscarPorBarcode() throws Exception {
        when(productService.findByBarcode("7891234560001")).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/search/barcode").with(user("admin").roles("ADMIN")).param("barcode", "7891234560001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Paracetamol 500mg")));
    }

    @Test
    @DisplayName("GET /api/products/search/name - deve buscar por nome")
    void findByName_deveBuscarPorNome() throws Exception {
        when(productService.findByName("Paracetamol")).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/search/name").with(user("admin").roles("ADMIN")).param("name", "Paracetamol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/products/category/{id} - deve buscar por categoria")
    void findByCategory_deveBuscarPorCategoria() throws Exception {
        when(productService.findByCategory(1L)).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/category/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/products/expired - deve buscar produtos vencidos")
    void findExpiredProducts_deveBuscarVencidos() throws Exception {
        when(productService.findExpiredProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products/expired").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/products/low-stock - deve buscar produtos com estoque baixo")
    void findLowStockProducts_deveBuscarEstoqueBaixo() throws Exception {
        when(productService.findLowStockProducts(10)).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/low-stock").with(user("admin").roles("ADMIN")).param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ======================== POST ========================

    @Test
    @DisplayName("POST /api/products - deve criar produto com sucesso")
    void create_deveCriarProduto() throws Exception {
        when(productService.save(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Paracetamol 500mg")));
    }

    @Test
    @DisplayName("POST /api/products - deve retornar 400 para validação inválida")
    void create_deveRetornar400ParaValidacaoInvalida() throws Exception {
        when(productService.save(any(Product.class)))
                .thenThrow(new IllegalArgumentException("O preço unitário não pode ser zero ou negativo"));

        mockMvc.perform(post("/api/products")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("O preço unitário não pode ser zero ou negativo")));
    }

    @Test
    @DisplayName("POST /api/products - deve retornar 403 sem autenticação")
    void create_deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/products - deve retornar 403 para role sem permissão")
    void create_deveRetornar403ParaRoleSemPermissao() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(user("customer").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden());
    }

    // ======================== PUT ========================

    @Test
    @DisplayName("PUT /api/products/{id} - deve atualizar produto com sucesso")
    void update_deveAtualizarProduto() throws Exception {
        when(productService.update(eq(1L), any(Product.class))).thenReturn(product);

        mockMvc.perform(put("/api/products/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Paracetamol 500mg")));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - deve retornar 400 para validação inválida")
    void update_deveRetornar400ParaValidacaoInvalida() throws Exception {
        when(productService.update(eq(1L), any(Product.class)))
                .thenThrow(new IllegalArgumentException("Código de barras já cadastrado"));

        mockMvc.perform(put("/api/products/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Código de barras já cadastrado")));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - deve retornar 404 quando não encontrado")
    void update_deveRetornar404QuandoNaoEncontrado() throws Exception {
        when(productService.update(eq(99L), any(Product.class)))
                .thenThrow(new RuntimeException("Produto não encontrado"));

        mockMvc.perform(put("/api/products/99")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isNotFound());
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE /api/products/{id} - deve deletar produto com sucesso")
    void delete_deveDeletarProduto() throws Exception {
        doNothing().when(productService).deleteById(1L);

        mockMvc.perform(delete("/api/products/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - deve retornar 404 quando não encontrado")
    void delete_deveRetornar404QuandoNaoEncontrado() throws Exception {
        doThrow(new RuntimeException("Produto não encontrado")).when(productService).deleteById(99L);

        mockMvc.perform(delete("/api/products/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
}
