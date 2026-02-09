package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.EquipoInscripcionDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipoInscripcionServiceTest {

    @Mock private CategoriaTorneoRepository categoriaRepo;
    @Mock private RobotRepository robotRepo;
    @Mock private EquipoTorneoRepository equipoRepo;
    @Mock private ClubRepository clubRepo;

    @InjectMocks
    private EquipoInscripcionService service;

    @Test
    void inscribirEquipo_ok() {
        EquipoInscripcionDTO dto = new EquipoInscripcionDTO();
        dto.setIdCategoriaTorneo("CAT1");
        dto.setNombreEquipo("Equipo A");
        dto.setRobots(List.of("R1"));

        Club club = Club.builder().idClub("C1").build();
        Torneo torneo = Torneo.builder()
                .idTorneo("T1")
                .fechaAperturaInscripcion(new Date(System.currentTimeMillis() - 1000))
                .fechaCierreInscripcion(new Date(System.currentTimeMillis() + 100000))
                .build();
        CategoriaTorneo categoria = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT1")
                .torneo(torneo)
                .modalidad(ModalidadCategoria.EQUIPO)
                .maxIntegrantesEquipo(3)
                .maxEquipos(5)
                .build();

        Usuario u = Usuario.builder().idUsuario("U1").build();
        Competidor comp = Competidor.builder()
                .idCompetidor("U1")
                .usuario(u)
                .clubActual(club)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();
        Robot robot = Robot.builder()
                .idRobot("R1")
                .nombre("R1")
                .estado(EstadoRobot.ACTIVO)
                .competidor(comp)
                .build();

        when(clubRepo.findById("C1")).thenReturn(Optional.of(club));
        when(categoriaRepo.findById("CAT1")).thenReturn(Optional.of(categoria));
        when(equipoRepo.existsByNombreIgnoreCaseAndCategoriaTorneoIdCategoriaTorneo("Equipo A", "CAT1")).thenReturn(false);
        when(robotRepo.findById("R1")).thenReturn(Optional.of(robot));
        when(equipoRepo.existsByRobotsIdRobotAndCategoriaTorneoTorneoIdTorneoAndEstadoNot("R1", "T1", EstadoEquipoTorneo.ANULADA)).thenReturn(false);
        when(equipoRepo.countByCategoriaTorneoIdCategoriaTorneoAndEstadoNot("CAT1", EstadoEquipoTorneo.ANULADA)).thenReturn(0L);
        when(equipoRepo.save(any(EquipoTorneo.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipoTorneo eq = service.inscribirEquipo("C1", dto);

        assertEquals("Equipo A", eq.getNombre());
    }

    @Test
    void anularEquipo_cambia_estado() {
        EquipoTorneo equipo = EquipoTorneo.builder().idEquipo("E1").estado(EstadoEquipoTorneo.ACTIVADA).build();
        when(equipoRepo.findById("E1")).thenReturn(Optional.of(equipo));
        when(equipoRepo.save(any(EquipoTorneo.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipoTorneo res = service.anularEquipo("E1", "motivo");

        assertEquals(EstadoEquipoTorneo.ANULADA, res.getEstado());
    }
}
