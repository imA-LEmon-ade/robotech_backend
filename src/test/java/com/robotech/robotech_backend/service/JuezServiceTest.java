package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.entity.Juez;
import com.robotech.robotech_backend.repository.JuezRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JuezServiceTest {

    @Mock private JuezRepository juezRepository;

    @InjectMocks
    private JuezService juezService;

    @Test
    void listar_devuelve_todos() {
        when(juezRepository.findAllWithUsuario()).thenReturn(List.of(new Juez()));
        assertEquals(1, juezService.listar().size());
    }

    @Test
    void obtener_devuelve_opcional() {
        when(juezRepository.findById("J1")).thenReturn(Optional.of(new Juez()));
        assertEquals(true, juezService.obtener("J1").isPresent());
    }

    @Test
    void crear_guarda() {
        Juez juez = new Juez();
        when(juezRepository.save(juez)).thenReturn(juez);
        assertEquals(juez, juezService.crear(juez));
    }

    @Test
    void eliminar_llama_delete() {
        juezService.eliminar("J1");
        verify(juezRepository, times(1)).deleteById("J1");
    }
}
