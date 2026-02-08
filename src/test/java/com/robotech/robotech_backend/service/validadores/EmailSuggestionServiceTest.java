package com.robotech.robotech_backend.service.validadores;

import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSuggestionServiceTest {

    @Mock private UsuarioRepository usuarioRepo;
    @Mock private ClubRepository clubRepo;
    @Mock private EmailValidator emailValidator;

    @InjectMocks
    private EmailSuggestionService service;

    @Test
    void sugerirCorreos_no_conflicto_retorna_vacio() {
        when(usuarioRepo.existsByCorreoIgnoreCase("test@example.com")).thenReturn(false);
        when(clubRepo.existsByCorreoContacto("test@example.com")).thenReturn(false);

        List<String> result = service.sugerirCorreosHumanosDisponibles("test@example.com", "Ana", "Perez", 3);

        assertEquals(0, result.size());
    }

    @Test
    void sugerirCorreos_con_conflicto_devuelve_sugerencias() {
        when(usuarioRepo.existsByCorreoIgnoreCase(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(clubRepo.existsByCorreoContacto(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(usuarioRepo.existsByCorreoIgnoreCase("test@example.com")).thenReturn(true);

        List<String> result = service.sugerirCorreosHumanosDisponibles("test@example.com", "Ana", "Perez", 2);

        assertEquals(2, result.size());
    }
}
