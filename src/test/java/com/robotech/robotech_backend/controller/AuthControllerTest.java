package com.robotech.robotech_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotech.robotech_backend.dto.PasswordResetConfirmDTO;
import com.robotech.robotech_backend.dto.PasswordResetRequestDTO;
import com.robotech.robotech_backend.exception.InvalidPasswordResetTokenException;
import com.robotech.robotech_backend.exception.UserNotFoundException;
import com.robotech.robotech_backend.exception.GlobalExceptionHandler; // Import GlobalExceptionHandler
import com.robotech.robotech_backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // New import
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration; // Import ValidationAutoConfiguration

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters
@Import({GlobalExceptionHandler.class, ValidationAutoConfiguration.class}) // Import only the exception handler and validation
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Additional mocks that AuthController depends on directly or indirectly
    @MockBean private com.robotech.robotech_backend.repository.UsuarioRepository usuarioRepository;
    @MockBean private com.robotech.robotech_backend.repository.ClubRepository clubRepository;
    @MockBean private com.robotech.robotech_backend.repository.CompetidorRepository competidorRepository;
    @MockBean private com.robotech.robotech_backend.repository.JuezRepository juezRepository;
    @MockBean private com.robotech.robotech_backend.service.CodigoRegistroService codigoRegistroService;
    @MockBean private com.robotech.robotech_backend.service.validadores.DniValidator dniValidator;
    @MockBean private com.robotech.robotech_backend.service.validadores.TelefonoValidator telefonoValidator;
    // Re-add mocks for SecurityConfig dependencies to satisfy context loading
    @MockBean private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @MockBean private com.robotech.robotech_backend.security.JwtService jwtService;
    @MockBean private com.robotech.robotech_backend.service.CustomUserDetailsService userDetailsService;
    @MockBean private com.robotech.robotech_backend.security.JwtAuthFilter jwtAuthFilter;


    private PasswordResetRequestDTO resetRequestDTO;
    private PasswordResetConfirmDTO resetConfirmDTO;

    @BeforeEach
    void setUp() {
        resetRequestDTO = new PasswordResetRequestDTO();
        resetRequestDTO.setEmail("test@example.com");

        resetConfirmDTO = new PasswordResetConfirmDTO();
        resetConfirmDTO.setToken("valid-token");
        resetConfirmDTO.setNewPassword("NewSecurePass123!");
    }

    // --- requestPasswordReset Endpoint Tests ---
    @Test
    void requestPasswordReset_ValidEmail_ReturnsOk() throws Exception {
        doNothing().when(authService).requestPasswordReset(anyString());

        mockMvc.perform(post("/api/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequestDTO)))
                .andExpect(status().isOk());

        verify(authService, times(1)).requestPasswordReset(resetRequestDTO.getEmail());
    }

    @Test
    void requestPasswordReset_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        resetRequestDTO.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        verify(authService, never()).requestPasswordReset(anyString());
    }

    @Test
    void requestPasswordReset_UserNotFound() throws Exception {
        doThrow(new UserNotFoundException("Usuario no encontrado")).when(authService).requestPasswordReset(anyString());

        mockMvc.perform(post("/api/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));

        verify(authService, times(1)).requestPasswordReset(resetRequestDTO.getEmail());
    }


    // --- resetPassword Endpoint Tests ---
    @Test
    void resetPassword_ValidRequest_ReturnsOk() throws Exception {
        doNothing().when(authService).resetPassword(anyString(), anyString());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetConfirmDTO)))
                .andExpect(status().isOk());

        verify(authService, times(1)).resetPassword(resetConfirmDTO.getToken(), resetConfirmDTO.getNewPassword());
    }

    @Test
    void resetPassword_InvalidNewPasswordSize_ReturnsBadRequest() throws Exception {
        resetConfirmDTO.setNewPassword("short"); // Less than 8 characters

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetConfirmDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.newPassword").exists());

        verify(authService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void resetPassword_InvalidOrExpiredToken_ReturnsBadRequest() throws Exception {
        doThrow(new InvalidPasswordResetTokenException("Token inválido")).when(authService).resetPassword(anyString(), anyString());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetConfirmDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_RESET_TOKEN"))
                .andExpect(jsonPath("$.message").value("Token inválido"));

        verify(authService, times(1)).resetPassword(resetConfirmDTO.getToken(), resetConfirmDTO.getNewPassword());
    }
}