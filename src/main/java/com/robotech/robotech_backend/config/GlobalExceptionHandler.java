package com.robotech.robotech_backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Manejo de duplicados (correo, teléfono, dni, etc)
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<?> handleSQLIntegrity(SQLIntegrityConstraintViolationException ex) {

        String message = ex.getMessage();

        if (message.contains("correo"))
            return ResponseEntity.badRequest().body("El correo ya está registrado");

        if (message.contains("telefono"))
            return ResponseEntity.badRequest().body("El teléfono ya está registrado");

        if (message.contains("dni"))
            return ResponseEntity.badRequest().body("El DNI ya está registrado");

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Error de integridad de datos");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno: " + ex.getMessage());
    }
}
