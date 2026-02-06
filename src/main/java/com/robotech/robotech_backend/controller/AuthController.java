package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.*;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import com.robotech.robotech_backend.service.AuthService;
import com.robotech.robotech_backend.service.CodigoRegistroService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    // -------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                authService.login(request.getCorreo(), request.getContrasena())
        );
    }

    // -------------------------------------------------------
    // SOLICITAR RESTABLECIMIENTO DE CONTRASEÑA
    // -------------------------------------------------------
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok("Si el correo electrónico está registrado, se ha enviado un enlace de restablecimiento de contraseña.");
    }

    // -------------------------------------------------------
    // RESTABLECER CONTRASEÑA
    // -------------------------------------------------------
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Contraseña restablecida exitosamente.");
    }


    // -------------------------------------------------------
    // REGISTRO COMPETIDOR
    // -------------------------------------------------------
    @PostMapping("/registro/competidor")
    public ResponseEntity<?> registrarCompetidor(
            @RequestBody RegistroCompetidorDTO dto) {

        authService.registrarCompetidor(dto);

        return ResponseEntity.ok(
                "Registro enviado. El club debe aprobar tu ingreso."
        );
    }

    // -------------------------------------------------------
    // REGISTRO CLUB
    // -------------------------------------------------------
    @PostMapping("/registro/club")
    public ResponseEntity<?> registrarClub(
            @RequestBody RegistroClubDTO dto) {

        authService.registrarClub(dto);

        return ResponseEntity.ok(
                "Solicitud enviada. Pendiente de aprobación del administrador."
        );
    }

    // -------------------------------------------------------
    // REGISTRO JUEZ
    // -------------------------------------------------------
    @PostMapping("/registro/juez")
    public ResponseEntity<?> registrarJuez(
            @RequestBody RegistroJuezDTO dto) {

        authService.registrarJuez(dto);

        return ResponseEntity.ok(
                "Solicitud enviada. Pendiente de validación del admin."
        );
    }
}


