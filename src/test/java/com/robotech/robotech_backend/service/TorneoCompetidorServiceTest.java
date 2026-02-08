package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ResultadoTorneoDTO;
import com.robotech.robotech_backend.dto.TorneoCompetidorDTO;
import com.robotech.robotech_backend.model.entity.CategoriaTorneo;
import com.robotech.robotech_backend.model.entity.EquipoTorneo;
import com.robotech.robotech_backend.model.entity.InscripcionTorneo;
import com.robotech.robotech_backend.model.entity.Robot;
import com.robotech.robotech_backend.model.entity.Torneo;
import com.robotech.robotech_backend.model.enums.CategoriaCompetencia;
import com.robotech.robotech_backend.model.enums.EstadoEquipoTorneo;
import com.robotech.robotech_backend.model.enums.EstadoInscripcion;
import com.robotech.robotech_backend.model.enums.ModalidadCategoria;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.HistorialCalificacionRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TorneoCompetidorServiceTest {

    @Mock private InscripcionTorneoRepository inscripcionRepo;
    @Mock private EquipoTorneoRepository equipoRepo;
    @Mock private HistorialCalificacionRepository historialRepo;

    @InjectMocks
    private TorneoCompetidorService torneoCompetidorService;

    @Test
    void listarMisTorneos_combina_individual_y_equipo() {
        Torneo torneo = Torneo.builder()
                .idTorneo("T1")
                .nombre("Torneo Nacional")
                .estado("FINALIZADO")
                .fechaInicio(new Date())
                .fechaFin(new Date())
                .build();

        CategoriaTorneo cat = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT1")
                .torneo(torneo)
                .categoria(CategoriaCompetencia.MINISUMO)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .build();

        Robot robot = Robot.builder().nombre("Titan").build();
        InscripcionTorneo ins = InscripcionTorneo.builder()
                .categoriaTorneo(cat)
                .robot(robot)
                .estado(EstadoInscripcion.ACTIVADA)
                .build();

        EquipoTorneo eq = EquipoTorneo.builder()
                .categoriaTorneo(cat)
                .robots(List.of(robot))
                .estado(EstadoEquipoTorneo.ACTIVADA)
                .build();

        when(inscripcionRepo.findByRobotCompetidorUsuarioIdUsuario("U1")).thenReturn(List.of(ins));
        when(equipoRepo.findByRobotsCompetidorUsuarioIdUsuario("U1")).thenReturn(List.of(eq));
        when(historialRepo.obtenerRankingRobots("T1")).thenReturn(List.of(new ResultadoTorneoDTO("Titan", 90L)));

        List<TorneoCompetidorDTO> result = torneoCompetidorService.listarMisTorneos("U1");

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getInscripciones());
        assertEquals("Titan", result.get(0).getGanador());
    }
}
