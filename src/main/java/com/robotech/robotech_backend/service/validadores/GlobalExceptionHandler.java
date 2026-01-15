package com.robotech.robotech_backend.service.validadores;

import com.robotech.robotech_backend.dto.ApiErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // EMAIL DUPLICADO CON SUGERENCIAS (FRONT PRO)
    // =========================
    @ExceptionHandler(EmailTakenException.class)
    public ResponseEntity<ApiErrorDTO> handleEmailTaken(EmailTakenException ex) {

        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("EMAIL_TAKEN");
        error.setMessage(ex.getMessage());
        error.setFieldErrors(Map.of("field", ex.getField()));
        error.setSuggestions(ex.getSuggestions());

        return ResponseEntity.badRequest().body(error); // 400
    }

    // =========================
    // VALIDACIONES @Valid (DTO)
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, Object> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("VALIDATION_ERROR");
        error.setMessage("Revisa el formulario");
        error.setFieldErrors(errors);
        error.setSuggestions(List.of());

        return ResponseEntity.badRequest().body(error); // 400
    }

    // =========================
    // ERRORES DE INTEGRIDAD DB (BACKUP)
    // =========================
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleSQLIntegrity(SQLIntegrityConstraintViolationException ex) {

        String message = ex.getMessage().toLowerCase();

        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("DATA_INTEGRITY_ERROR");
        error.setSuggestions(List.of());

        if (message.contains("correo")) {
            error.setMessage("El correo ya está registrado");
            error.setFieldErrors(Map.of("correo", "duplicado"));
            return ResponseEntity.badRequest().body(error);
        }

        if (message.contains("telefono")) {
            error.setMessage("El teléfono ya está registrado");
            error.setFieldErrors(Map.of("telefono", "duplicado"));
            return ResponseEntity.badRequest().body(error);
        }

        if (message.contains("dni")) {
            error.setMessage("El DNI ya está registrado");
            error.setFieldErrors(Map.of("dni", "duplicado"));
            return ResponseEntity.badRequest().body(error);
        }

        error.setMessage("Error de integridad de datos");
        error.setFieldErrors(Map.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // =========================
    // FALLBACK GENERAL
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGeneral(Exception ex) {

        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("INTERNAL_ERROR");
        error.setMessage("Error interno del servidor");
        error.setFieldErrors(Map.of());
        error.setSuggestions(List.of());

        // 🔴 en producción NO expongas ex.getMessage()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
