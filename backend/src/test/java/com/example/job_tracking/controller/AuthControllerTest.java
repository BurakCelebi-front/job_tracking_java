package com.example.job_tracking.controller;

/*
 * Varsayımlar / notlar:
 * - Spring Boot 4.1.1'de @MockBean deprecated -> @MockitoBean kullanıldı (org.springframework.test.context.bean.override.mockito)
 * - @WebMvcTest paketi de değişti: org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
 *   (spring-boot-starter-webmvc-test zaten pom.xml'de var)
 * - @AutoConfigureMockMvc(addFilters = false) ile security filter chain'i devre dışı bıraktım —
 *   yoksa @WebMvcTest varsayılan Spring Security auto-config'i devreye girip JwtService/CustomUserDetailsService
 *   bean'lerini isteyebilir ya da default deny-all kuralı /api/auth/** dahil her şeyi 401/403'e düşürebilir.
 *   Yani bu test SecurityConfig'teki permitAll kurallarını DEĞİL, sadece controller<->service sözleşmesini test ediyor.
 * - RegisterRequest/LoginRequest'te @NotBlank/@Email gibi validasyon anotasyonları OLDUĞUNU varsaydım
 *   (controller @Valid kullanıyor). Yoksa "missing email" testi 400 yerine 200 döner, haber ver.
 * - IllegalArgumentException (email zaten var) için global bir @ControllerAdvice olup olmadığını bilmiyorum,
 *   o yüzden bu senaryoyu buraya eklemedim — varsa söyle, ona göre status kodunu test ederiz.
 * - DÜZELTME 1: addFilters=false sadece MockMvc'nin filter'ları ÇALIŞTIRMASINI engelliyor, filter bean'inin
 *   context'te OLUŞTURULMASINI değil. JwtAuthenticationFilter @Component + Filter olduğu için slice onu
 *   yine de instantiate etmeye çalışıp JwtService bulamayınca patlıyordu. excludeFilters ile bu bean'i
 *   component scan'den tamamen çıkardım, artık hiç oluşturulmuyor.
 * - DÜZELTME 2: Spring Boot 4 / Jackson 3'te com.fasterxml.jackson.databind.ObjectMapper artık autoconfigure
 *   edilmiyor, yerine tools.jackson.databind.json.JsonMapper bean'i geliyor (paket de değişti). ObjectMapper
 *   yerine JsonMapper enjekte ettim, writeValueAsString metodu aynı isimle JsonMapper'da da var.
 */

import com.example.job_tracking.dto.AuthResponse;
import com.example.job_tracking.dto.LoginRequest;
import com.example.job_tracking.dto.RegisterRequest;
import com.example.job_tracking.security.JwtAuthenticationFilter;
import com.example.job_tracking.service.AuthService;
import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private AuthService authService;

    // ---------- POST /api/auth/register ----------

    @Test
    void register_validRequest_returns200WithToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullname("Burak");
        request.setEmail("burak@example.com");
        request.setPassword("secret123");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("jwt-token-value"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-value"));
    }

    @Test
    void register_missingEmail_returns400AndDoesNotCallService() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullname("Burak");
        request.setPassword("secret123");
        // email boş bırakıldı

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    // ---------- POST /api/auth/login ----------

    @Test
    void login_validRequest_returns200WithToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("burak@example.com");
        request.setPassword("secret123");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("jwt-token-value"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-value"));
    }

    @Test
    void login_missingPassword_returns400AndDoesNotCallService() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("burak@example.com");
        // password boş bırakıldı

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}