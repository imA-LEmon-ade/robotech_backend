package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ResultadoTorneoDTO;
import com.robotech.robotech_backend.dto.TorneoPublicoDTO;
import com.robotech.robotech_backend.model.entity.CategoriaTorneo;
import com.robotech.robotech_backend.model.entity.Torneo;
import com.robotech.robotech_backend.model.enums.CategoriaCompetencia;
import com.robotech.robotech_backend.repository.HistorialCalificacionRepository;
import com.robotech.robotech_backend.repository.TorneoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TorneoPublicoServiceTest {

    @Mock private TorneoRepository torneoRepo;
    @Mock private HistorialCalificacionRepository historialRepo;

    @InjectMocks
    private TorneoPublicoService torneoPublicoService;

    @Test
    void obtenerTodos_finalizado_incluye_ganador() {
        Torneo t = Torneo.builder()
                .idTorneo("T1")
                .nombre("Torneo Nacional")
                .estado("FINALIZADO")
                .categorias(List.of(CategoriaTorneo.builder().categoria(CategoriaCompetencia.MINISUMO).build()))
                .build();

        when(torneoRepo.findByEstadoIn(org.mockito.ArgumentMatchers.anyList())).thenReturn(List.of(t));
        when(historialRepo.obtenerRankingRobots("T1")).thenReturn(List.of(new ResultadoTorneoDTO("Robot1", 90L)));

        List<TorneoPublicoDTO> result = torneoPublicoService.obtenerTodos();

        assertEquals(1, result.size());
        assertEquals("Robot1", result.get(0).getGanador());
    }
}
