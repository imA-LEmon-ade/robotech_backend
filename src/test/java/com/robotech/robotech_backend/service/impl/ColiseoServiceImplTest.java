package com.robotech.robotech_backend.service.impl;

import com.robotech.robotech_backend.dto.ColiseoDTO;
import com.robotech.robotech_backend.model.entity.Coliseo;
import com.robotech.robotech_backend.repository.ColiseoRepository;
import com.robotech.robotech_backend.service.impl.ColiseoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColiseoServiceImplTest {

    @Mock private ColiseoRepository coliseoRepository;

    @InjectMocks
    private ColiseoServiceImpl service;

    @Test
    void crear_ok() {
        ColiseoDTO dto = new ColiseoDTO();
        dto.setNombre("Coliseo A");
        dto.setUbicacion("Lima");

        when(coliseoRepository.save(any(Coliseo.class))).thenAnswer(inv -> inv.getArgument(0));

        ColiseoDTO resp = service.crear(dto);

        assertEquals("Coliseo A", resp.getNombre());
    }

    @Test
    void listar_ok() {
        Coliseo c = new Coliseo();
        c.setIdColiseo("C1");
        c.setNombre("Coliseo A");
        c.setUbicacion("Lima");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Coliseo> page = new PageImpl<>(List.of(c), pageable, 1);
        when(coliseoRepository.buscar("lim", pageable)).thenReturn(page);

        Page<ColiseoDTO> result = service.listar(pageable, "lim");

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void editar_ok() {
        Coliseo c = new Coliseo();
        c.setIdColiseo("C1");
        c.setNombre("Coliseo A");
        c.setUbicacion("Lima");

        when(coliseoRepository.findById("C1")).thenReturn(Optional.of(c));
        when(coliseoRepository.save(any(Coliseo.class))).thenAnswer(inv -> inv.getArgument(0));

        ColiseoDTO dto = new ColiseoDTO();
        dto.setNombre("Coliseo B");
        dto.setUbicacion("Cusco");

        ColiseoDTO resp = service.editar("C1", dto);

        assertEquals("Coliseo B", resp.getNombre());
    }

    @Test
    void eliminar_ok() {
        service.eliminar("C1");
        verify(coliseoRepository, times(1)).deleteById("C1");
    }
}
