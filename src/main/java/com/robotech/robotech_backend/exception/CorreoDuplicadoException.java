package com.robotech.robotech_backend.exception;

import java.util.List;

public class CorreoDuplicadoException extends RuntimeException {
    private final String field;          // "correoPropietario" o "correoContacto"
    private final List<String> sugerencias;

    public CorreoDuplicadoException(String field, String message, List<String> sugerencias) {
        super(message);
        this.field = field;
        this.sugerencias = sugerencias;
    }

    public String getField() { return field; }
    public List<String> getSugerencias() { return sugerencias; }
}
