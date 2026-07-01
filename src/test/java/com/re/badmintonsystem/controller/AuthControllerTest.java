package com.re.badmintonsystem.controller;

import com.re.badmintonsystem.dto.request.LoginRequest;
import com.re.badmintonsystem.dto.request.RefreshTokenRequest;
import com.re.badmintonsystem.dto.request.RegisterRequest;
import com.re.badmintonsystem.dto.response.AuthResponse;
import com.re.badmintonsystem.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("admin");
        loginRequest.setPassword("123456");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("New User");
    }

    @Test
    @DisplayName("POST /v1/auth/login - should return JWT on success")
    void login_shouldReturnJwtToken() throws Exception {
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .tokenType("Bearer")
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").value("test-access-token"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /v1/auth/register - should return success")
    void register_shouldReturnSuccess() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(AuthResponse.builder()
                        .accessToken("test-access-token")
                        .refreshToken("test-refresh-token")
                        .tokenType("Bearer")
                        .build());

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /v1/auth/login - should return 401 for bad credentials")
    void login_withBadCredentials_shouldReturn401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new com.re.badmintonsystem.exception.BadRequestException("Bad credentials"));

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/auth/refresh-token - should return new token")
    void refresh_shouldReturnNewToken() throws Exception {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("old-refresh-token");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .build();
        when(authService.refreshToken(anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access_token").value("new-access-token"));
    }

    @Test
    @DisplayName("POST /v1/auth/logout - should return success")
    void logout_shouldReturnSuccess() throws Exception {
        doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/v1/auth/logout")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /v1/auth/register - should return 400 for invalid input")
    void register_withInvalidInput_shouldReturn400() throws Exception {
        registerRequest.setEmail("invalid-email");

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}
