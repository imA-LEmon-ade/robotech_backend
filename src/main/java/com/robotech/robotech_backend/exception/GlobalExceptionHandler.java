package com.robotech.robotech_backend.exception;

import com.robotech.robotech_backend.dto.ApiErrorDTO;
import com.robotech.robotech_backend.service.validadores.FieldValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.server.ResponseStatusException; // IMPORTANTE

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // 🔥 NUEVO: MANEJO DE ResponseStatusException (Para RobotService)
    // =========================
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorDTO> handleResponseStatus(ResponseStatusException ex) {

        ApiErrorDTO error = new ApiErrorDTO();
        // Usamos el código de estado de la excepción (ej: CONFLICT, BAD_REQUEST)
        error.setCode(ex.getStatusCode().toString());

        // Usamos el mensaje real que pusimos en el servicio ("El nickname ya existe")
        error.setMessage(ex.getReason());

        error.setFieldErrors(Map.of());
        error.setSuggestions(List.of());

        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    // =========================
    // Manejo de UserNotFoundException
    // =========================
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleUserNotFoundException(UserNotFoundException ex) {
        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("USER_NOT_FOUND");
        error.setMessage(ex.getMessage());
        error.setFieldErrors(Map.of());
        error.setSuggestions(List.of("Verifica el correo electrónico ingresado."));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // =========================
    // Manejo de InvalidPasswordResetTokenException
    // =========================
    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ApiErrorDTO> handleInvalidPasswordResetTokenException(InvalidPasswordResetTokenException ex) {
        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("INVALID_RESET_TOKEN");
        error.setMessage(ex.getMessage());
        error.setFieldErrors(Map.of());
        error.setSuggestions(List.of("El enlace de restablecimiento es inválido o ha caducado. Solicita uno nuevo."));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // =========================
    // EMAIL DUPLICADO (Tu código existente)
    // =========================
    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ApiErrorDTO> handleFieldValidation(FieldValidationException ex) {
        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("FIELD_VALIDATION");
        error.setMessage(ex.getMessage());
        error.setFieldErrors(Map.of("field", ex.getField()));
        error.setSuggestions(ex.getSuggestions());
        return ResponseEntity.badRequest().body(error);
    }

    // =========================
    // VALIDACIONES @Valid (Tu código existente)
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), String.valueOf(err.getDefaultMessage())));

        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("VALIDATION_ERROR");
        error.setMessage("Revisa el formulario");
        error.setFieldErrors(errors);
        error.setSuggestions(List.of());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDTO> handleUnreadableBody(HttpMessageNotReadableException ex) {
        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("BAD_REQUEST");
        error.setMessage("Body JSON invalido o faltante");
        error.setFieldErrors(Map.of());
        error.setSuggestions(List.of("Envia un JSON valido con Content-Type: application/json"));
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorDTO> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("METHOD_NOT_ALLOWED");
        error.setMessage("Metodo HTTP no permitido para esta ruta");
        error.setFieldErrors(Map.of());
        error.setSuggestions(List.of("Usa el metodo HTTP correcto para el endpoint"));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    // =========================
    // ERRORES DB (Tu código existente)
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
    // FALLBACK GENERAL (Tu código existente)
    // =========================
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorDTO> handleRuntime(RuntimeException ex) {
        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("BUSINESS_ERROR");
        error.setMessage(ex.getMessage());
        error.setFieldErrors(Map.of());
        error.setSuggestions(List.of());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGeneral(Exception ex) {
        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("INTERNAL_ERROR");
        // En producción es mejor mensaje genérico, en desarrollo puedes usar ex.getMessage() para debug
        error.setMessage("Error interno del servidor");
        error.setFieldErrors(Map.of());
        error.setSuggestions(List.of());

        // Imprimir el error en consola para que tú lo veas
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}


