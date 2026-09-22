package com.kewen.GerenciamentoFarmacia.controllers;

import com.kewen.GerenciamentoFarmacia.entities.SaleProduct;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.SaleProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.context.annotation.Import(SecurityConfig.class)
@WebMvcTest(SaleProductController.class)
class SaleProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SaleProductService saleProductService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private SaleProduct saleProduct;

    @BeforeEach
    void setUp() {
        saleProduct = new SaleProduct();
        saleProduct.setId(1L);
        saleProduct.setQuantity(5L);
        saleProduct.setUnitPrice(new BigDecimal("25.00"));
    }

    // ======================== GET ========================

    @Test
    @DisplayName("GET /api/sale-products - deve retornar lista de itens de venda")
    void findAll_deveRetornarLista() throws Exception {
        when(saleProductService.findAll()).thenReturn(List.of(saleProduct));

        mockMvc.perform(get("/api/sale-products").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].quantity", is(5)));
    }

    @Test
    @DisplayName("GET /api/sale-products/{id} - deve retornar item por id")
    void findById_deveRetornarItem() throws Exception {
        when(saleProductService.findById(1L)).thenReturn(Optional.of(saleProduct));

        mockMvc.perform(get("/api/sale-products/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)));
    }

    @Test
    @DisplayName("GET /api/sale-products/{id} - deve retornar 404 quando não encontrado")
    void findById_deveRetornar404() throws Exception {
        when(saleProductService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sale-products/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/sale-products/search/sale/{saleId} - deve buscar por venda")
    void findBySaleId_deveBuscar() throws Exception {
        when(saleProductService.findBySaleId(1L)).thenReturn(List.of(saleProduct));

        mockMvc.perform(get("/api/sale-products/search/sale/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/sale-products/search/product/{productId} - deve buscar por produto")
    void findByProductId_deveBuscar() throws Exception {
        when(saleProductService.findByProductId(1L)).thenReturn(List.of(saleProduct));

        mockMvc.perform(get("/api/sale-products/search/product/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE /api/sale-products/{id} - deve deletar item com sucesso")
    void delete_deveDeletarItem() throws Exception {
        doNothing().when(saleProductService).deleteById(1L);

        mockMvc.perform(delete("/api/sale-products/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/sale-products/{id} - deve deletar com role EMPLOYEE")
    void delete_deveDeletarComoEmployee() throws Exception {
        doNothing().when(saleProductService).deleteById(1L);

        mockMvc.perform(delete("/api/sale-products/1").with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/sale-products/{id} - deve retornar 404 quando não encontrado")
    void delete_deveRetornar404() throws Exception {
        doThrow(new RuntimeException("Item não encontrado")).when(saleProductService).deleteById(99L);

        mockMvc.perform(delete("/api/sale-products/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/sale-products/{id} - deve retornar 403 para role sem permissão")
    void delete_deveRetornar403() throws Exception {
        mockMvc.perform(delete("/api/sale-products/1").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/sale-products/{id} - deve retornar 403 sem autenticação")
    void delete_deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(delete("/api/sale-products/1"))
                .andExpect(status().isForbidden());
    }
}
