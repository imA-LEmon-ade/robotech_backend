package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorDTO {
    private String code;
    private String message;
    private Map<String, String> fieldErrors;
    private List<String> suggestions;
}

