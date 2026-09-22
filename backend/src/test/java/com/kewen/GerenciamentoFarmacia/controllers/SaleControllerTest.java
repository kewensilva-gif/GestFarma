package com.kewen.GerenciamentoFarmacia.controllers;

import tools.jackson.databind.ObjectMapper;
import com.kewen.GerenciamentoFarmacia.entities.*;
import com.kewen.GerenciamentoFarmacia.enums.PaymentMethodEnum;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.config.SecurityConfig;
import com.kewen.GerenciamentoFarmacia.services.SaleService;
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
@WebMvcTest(SaleController.class)
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SaleService saleService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.kewen.GerenciamentoFarmacia.security.TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Sale sale;

    @BeforeEach
    void setUp() {
        sale = new Sale();
        sale.setId(1L);
        sale.setTotalPrice(new BigDecimal("150.00"));
        sale.setDiscount(new BigDecimal("10.00"));
        sale.setPaymentMethod(PaymentMethodEnum.PIX);
        sale.setEnabled(true);
    }

    // ======================== GET ========================

    @Test
    @DisplayName("GET /api/sales - deve retornar lista de vendas")
    void findAll_deveRetornarListaDeVendas() throws Exception {
        when(saleService.findAll()).thenReturn(List.of(sale));

        mockMvc.perform(get("/api/sales").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].totalPrice", is(150.00)));
    }

    @Test
    @DisplayName("GET /api/sales/{id} - deve retornar venda por id")
    void findById_deveRetornarVenda() throws Exception {
        when(saleService.findById(1L)).thenReturn(Optional.of(sale));

        mockMvc.perform(get("/api/sales/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice", is(150.00)));
    }

    @Test
    @DisplayName("GET /api/sales/{id} - deve retornar 404 quando não encontrada")
    void findById_deveRetornar404() throws Exception {
        when(saleService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sales/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/sales/search/payment-method - deve buscar por método de pagamento")
    void findByPaymentMethod_deveBuscarPorMetodo() throws Exception {
        when(saleService.findByPaymentMethod(PaymentMethodEnum.PIX)).thenReturn(List.of(sale));

        mockMvc.perform(get("/api/sales/search/payment-method").with(user("admin").roles("ADMIN")).param("paymentMethod", "PIX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/sales/search/price-greater - deve buscar por preço maior que")
    void findByPriceGreaterThan_deveBuscar() throws Exception {
        when(saleService.findByPriceGreaterThan(new BigDecimal("100.00"))).thenReturn(List.of(sale));

        mockMvc.perform(get("/api/sales/search/price-greater").with(user("admin").roles("ADMIN")).param("price", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/sales/search/price-less - deve buscar por preço menor que")
    void findByPriceLessThan_deveBuscar() throws Exception {
        when(saleService.findByPriceLessThan(new BigDecimal("200.00"))).thenReturn(List.of(sale));

        mockMvc.perform(get("/api/sales/search/price-less").with(user("admin").roles("ADMIN")).param("price", "200.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ======================== POST ========================

    @Test
    @DisplayName("POST /api/sales - deve criar venda com sucesso")
    void create_deveCriarVenda() throws Exception {
        when(saleService.save(any(Sale.class))).thenReturn(sale);

        mockMvc.perform(post("/api/sales")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sale)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice", is(150.00)));
    }

    @Test
    @DisplayName("POST /api/sales - deve criar venda com role EMPLOYEE")
    void create_deveCriarVendaComoEmployee() throws Exception {
        when(saleService.save(any(Sale.class))).thenReturn(sale);

        mockMvc.perform(post("/api/sales")
                        .with(user("employee").roles("EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sale)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/sales - deve retornar 400 para dados inválidos")
    void create_deveRetornar400() throws Exception {
        when(saleService.save(any(Sale.class)))
                .thenThrow(new IllegalArgumentException("O preço total deve ser maior que zero"));

        mockMvc.perform(post("/api/sales")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sale)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/sales - deve retornar 403 para role sem permissão")
    void create_deveRetornar403() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .with(user("customer").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sale)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/sales - deve retornar 403 sem autenticação")
    void create_deveRetornar403SemAuth() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sale)))
                .andExpect(status().isForbidden());
    }

    // ======================== PUT ========================

    @Test
    @DisplayName("PUT /api/sales/{id} - deve atualizar venda com sucesso")
    void update_deveAtualizarVenda() throws Exception {
        when(saleService.update(eq(1L), any(Sale.class))).thenReturn(sale);

        mockMvc.perform(put("/api/sales/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sale)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice", is(150.00)));
    }

    @Test
    @DisplayName("PUT /api/sales/{id} - deve retornar 400 para validação inválida")
    void update_deveRetornar400() throws Exception {
        when(saleService.update(eq(1L), any(Sale.class)))
                .thenThrow(new IllegalArgumentException("O desconto não pode ser maior que o preço total"));

        mockMvc.perform(put("/api/sales/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sale)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("PUT /api/sales/{id} - deve retornar 404 quando não encontrada")
    void update_deveRetornar404() throws Exception {
        when(saleService.update(eq(99L), any(Sale.class)))
                .thenThrow(new RuntimeException("Venda não encontrada"));

        mockMvc.perform(put("/api/sales/99")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sale)))
                .andExpect(status().isNotFound());
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("DELETE /api/sales/{id} - deve deletar venda com sucesso")
    void delete_deveDeletarVenda() throws Exception {
        doNothing().when(saleService).deleteById(1L);

        mockMvc.perform(delete("/api/sales/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/sales/{id} - deve retornar 404 quando não encontrada")
    void delete_deveRetornar404() throws Exception {
        doThrow(new RuntimeException("Venda não encontrada")).when(saleService).deleteById(99L);

        mockMvc.perform(delete("/api/sales/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
}
