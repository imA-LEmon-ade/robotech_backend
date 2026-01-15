package com.robotech.robotech_backend.service.validadores;

import java.util.List;

public class EmailTakenException extends RuntimeException {

    private final String field;
    private final List<String> suggestions;

    public EmailTakenException(String field, String message, List<String> suggestions) {
        super(message);
        this.field = field;
        this.suggestions = suggestions;
    }

    public String getField() {
        return field;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
