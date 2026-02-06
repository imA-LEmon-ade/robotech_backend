package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.service.validadores.EmailSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/sugerencias")
@RequiredArgsConstructor
public class SugerenciasController {

    private final EmailSuggestionService emailSuggestionService;

    @GetMapping("/emails")
    public List<String> sugerir(
            @RequestParam String correo,
            @RequestParam String nombres,
            @RequestParam String apellidos
    ) {
        return emailSuggestionService.sugerirCorreosHumanosDisponibles(
                correo, nombres, apellidos, 6
        );
    }
}


