package com.robotech.robotech_backend.service.validadores;

import com.robotech.robotech_backend.dto.ApiErrorDTO;
import com.robotech.robotech_backend.exception.CorreoDuplicadoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ValidationHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, Object> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity.badRequest().body(
                new ApiErrorDTO(
                        "VALIDATION_ERROR",
                        "Revisa el formulario",
                        errors,
                        List.of()
                )
        );
    }


    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ApiErrorDTO> handleCorreoDuplicado(CorreoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiErrorDTO(
                        "EMAIL_TAKEN",
                        ex.getMessage(),
                        Map.of("field", ex.getField()),
                        ex.getSugerencias()
                )
        );
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiErrorDTO("DUPLICATE_OR_INVALID", "Ya existe un registro con esos datos", Map.of(), List.of())
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorDTO> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(
                new ApiErrorDTO("BUSINESS_ERROR", ex.getMessage(), Map.of(), List.of())
        );
    }
}
