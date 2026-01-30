package com.robotech.robotech_backend.exception;

import com.robotech.robotech_backend.dto.ApiErrorDTO;
import com.robotech.robotech_backend.service.validadores.FieldValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

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
    // VALIDACIONES @Valid
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(err -> {
            errors.put(err.getField(), err.getDefaultMessage());
        });

        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("VALIDATION_ERROR");

        // MENSAJE GENERAL MÁS ÚTIL
        error.setMessage("Errores de validación");

        // LO IMPORTANTE
        error.setFieldErrors(errors);

        error.setSuggestions(List.of());

        return ResponseEntity.badRequest().body(error);
    }

    // =========================
    // FALLBACK GENERAL
    // =========================
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

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ApiErrorDTO> handleFieldValidation(FieldValidationException ex) {

        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("FIELD_VALIDATION_ERROR");

        // Mensaje principal (puede ser el mismo)
        error.setMessage(ex.getMessage());

        // 🔥 CLAVE PARA EL FRONT
        error.setFieldErrors(Map.of(
                ex.getField(), ex.getMessage()
        ));

        error.setSuggestions(ex.getSuggestions());

        return ResponseEntity.badRequest().body(error);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleDataIntegrity(DataIntegrityViolationException ex) {

        String message = ex.getMostSpecificCause().getMessage().toLowerCase();

        ApiErrorDTO error = new ApiErrorDTO();
        error.setCode("FIELD_VALIDATION_ERROR");
        error.setSuggestions(List.of());

        if (message.contains("correo")) {
            error.setMessage("El correo ya existe");
            error.setFieldErrors(Map.of("correo", "El correo ya existe"));
            return ResponseEntity.badRequest().body(error);
        }

        if (message.contains("telefono")) {
            error.setMessage("El teléfono ya existe");
            error.setFieldErrors(Map.of("telefono", "El teléfono ya existe"));
            return ResponseEntity.badRequest().body(error);
        }

        if (message.contains("dni")) {
            error.setMessage("El DNI ya existe");
            error.setFieldErrors(Map.of("dni", "El DNI ya existe"));
            return ResponseEntity.badRequest().body(error);
        }

        error.setMessage("Error de integridad de datos");
        error.setFieldErrors(Map.of());

        return ResponseEntity.badRequest().body(error);
    }

}
