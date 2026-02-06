package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.exception.InvalidPasswordResetTokenException;
import com.robotech.robotech_backend.exception.UserNotFoundException;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.security.JwtService; // Added missing import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenGeneratorService tokenGeneratorService;
    @Mock
    private EmailService emailService;

    // Other mocks that AuthService uses but are not directly relevant to password reset tests
    @Mock private CodigoRegistroService codigoRegistroService;
    @Mock private JwtService jwtService;
    @Mock private com.robotech.robotech_backend.repository.ClubRepository clubRepository;
    @Mock private com.robotech.robotech_backend.repository.CompetidorRepository competidorRepository;
    @Mock private com.robotech.robotech_backend.repository.JuezRepository juezRepository;
    @Mock private com.robotech.robotech_backend.service.validadores.DniValidator dniValidator;
    @Mock private com.robotech.robotech_backend.service.validadores.TelefonoValidator telefonoValidator;

    @InjectMocks
    private AuthService authService;

    private Usuario testUser;
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_TOKEN = "secureRandomToken";
    private final String NEW_PASSWORD = "newSecurePassword";
    private final String ENCODED_NEW_PASSWORD = "encodedNewSecurePassword";

    @BeforeEach
    void setUp() {
        testUser = new Usuario();
        testUser.setIdUsuario("user123");
        testUser.setCorreo(TEST_EMAIL);
        testUser.setContrasenaHash("oldEncodedPassword");
    }

    // --- requestPasswordReset Tests ---
    @Test
    void requestPasswordReset_UserFound_TokenGeneratedAndEmailSent() {
        when(usuarioRepository.findByCorreo(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(tokenGeneratorService.generateSecureToken()).thenReturn(TEST_TOKEN);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(testUser); // Mock save operation

        authService.requestPasswordReset(TEST_EMAIL);

        verify(usuarioRepository, times(1)).findByCorreo(TEST_EMAIL);
        verify(tokenGeneratorService, times(1)).generateSecureToken();
        assertNotNull(testUser.getPasswordResetToken());
        assertEquals(TEST_TOKEN, testUser.getPasswordResetToken());
        assertNotNull(testUser.getPasswordResetTokenExpiryDate());
        assertTrue(testUser.getPasswordResetTokenExpiryDate().isAfter(LocalDateTime.now()));
        verify(usuarioRepository, times(1)).save(testUser);
        verify(emailService, times(1)).sendPasswordResetEmail(TEST_EMAIL, TEST_TOKEN);
    }

    @Test
    void requestPasswordReset_UserNotFound_ThrowsUserNotFoundException() {
        when(usuarioRepository.findByCorreo(TEST_EMAIL)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () ->
                authService.requestPasswordReset(TEST_EMAIL));

        assertEquals("Usuario no encontrado con ese correo electrónico.", exception.getMessage());
        verify(usuarioRepository, times(1)).findByCorreo(TEST_EMAIL);
        verify(tokenGeneratorService, never()).generateSecureToken();
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- resetPassword Tests ---
    @Test
    void resetPassword_ValidToken_PasswordUpdatedAndTokenInvalidated() {
        testUser.setPasswordResetToken(TEST_TOKEN);
        testUser.setPasswordResetTokenExpiryDate(LocalDateTime.now().plusHours(1));

        when(usuarioRepository.findByPasswordResetToken(TEST_TOKEN)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(testUser); // Mock save operation

        authService.resetPassword(TEST_TOKEN, NEW_PASSWORD);

        verify(usuarioRepository, times(1)).findByPasswordResetToken(TEST_TOKEN);
        verify(passwordEncoder, times(1)).encode(NEW_PASSWORD);
        assertEquals(ENCODED_NEW_PASSWORD, testUser.getContrasenaHash());
        assertNull(testUser.getPasswordResetToken());
        assertNull(testUser.getPasswordResetTokenExpiryDate());
        verify(usuarioRepository, times(1)).save(testUser);
    }

    @Test
    void resetPassword_InvalidToken_ThrowsInvalidPasswordResetTokenException() {
        when(usuarioRepository.findByPasswordResetToken(TEST_TOKEN)).thenReturn(Optional.empty());

        Exception exception = assertThrows(InvalidPasswordResetTokenException.class, () ->
                authService.resetPassword(TEST_TOKEN, NEW_PASSWORD));

        assertEquals("Token de restablecimiento inválido.", exception.getMessage());
        verify(usuarioRepository, times(1)).findByPasswordResetToken(TEST_TOKEN);
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsInvalidPasswordResetTokenException() {
        testUser.setPasswordResetToken(TEST_TOKEN);
        testUser.setPasswordResetTokenExpiryDate(LocalDateTime.now().minusMinutes(1)); // Expired token

        when(usuarioRepository.findByPasswordResetToken(TEST_TOKEN)).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(InvalidPasswordResetTokenException.class, () ->
                authService.resetPassword(TEST_TOKEN, NEW_PASSWORD));

        assertEquals("Token de restablecimiento caducado.", exception.getMessage());
        verify(usuarioRepository, times(1)).findByPasswordResetToken(TEST_TOKEN);
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void resetPassword_TokenWithNullExpiryDate_ThrowsInvalidPasswordResetTokenException() {
        testUser.setPasswordResetToken(TEST_TOKEN);
        testUser.setPasswordResetTokenExpiryDate(null); // Null expiry date

        when(usuarioRepository.findByPasswordResetToken(TEST_TOKEN)).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(InvalidPasswordResetTokenException.class, () ->
                authService.resetPassword(TEST_TOKEN, NEW_PASSWORD));

        assertEquals("Token de restablecimiento caducado.", exception.getMessage());
        verify(usuarioRepository, times(1)).findByPasswordResetToken(TEST_TOKEN);
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
