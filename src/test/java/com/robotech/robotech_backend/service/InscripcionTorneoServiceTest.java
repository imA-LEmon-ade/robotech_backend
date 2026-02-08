package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.InscripcionIndividualDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InscripcionTorneoServiceTest {

    @Mock private CategoriaTorneoRepository categoriaRepo;
    @Mock private RobotRepository robotRepo;
    @Mock private InscripcionTorneoRepository inscripcionRepo;
    @Mock private ClubRepository clubRepo;
    @Mock private CompetidorRepository competidorRepo;

    @InjectMocks
    private InscripcionTorneoService service;

    @Test
    void inscribirIndividualComoClub_ok() {
        InscripcionIndividualDTO dto = new InscripcionIndividualDTO();
        dto.setIdCategoriaTorneo("CAT1");
        dto.setIdRobot("R1");

        Club club = Club.builder().idClub("C1").build();
        Torneo torneo = Torneo.builder()
                .idTorneo("T1")
                .fechaAperturaInscripcion(new Date(System.currentTimeMillis() - 1000))
                .fechaCierreInscripcion(new Date(System.currentTimeMillis() + 100000))
                .build();
        CategoriaTorneo categoria = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT1")
                .torneo(torneo)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .maxParticipantes(8)
                .build();

        Usuario u = Usuario.builder().idUsuario("U1").build();
        Competidor comp = Competidor.builder().idCompetidor("U1").usuario(u).clubActual(club).build();
        Robot robot = Robot.builder().idRobot("R1").competidor(comp).build();

        when(clubRepo.findByUsuario_IdUsuario("UCLUB")).thenReturn(Optional.of(club));
        when(categoriaRepo.findById("CAT1")).thenReturn(Optional.of(categoria));
        when(robotRepo.findById("R1")).thenReturn(Optional.of(robot));
        when(inscripcionRepo.existsByRobot_IdRobotAndCategoriaTorneo_Torneo_IdTorneoAndEstado("R1", "T1", EstadoInscripcion.ACTIVADA)).thenReturn(false);
        when(inscripcionRepo.countByCategoriaTorneoIdCategoriaTorneoAndEstado("CAT1", EstadoInscripcion.ACTIVADA)).thenReturn(0L);
        when(inscripcionRepo.save(any(InscripcionTorneo.class))).thenAnswer(inv -> inv.getArgument(0));

        InscripcionTorneo ins = service.inscribirIndividualComoClub("UCLUB", dto);

        assertNotNull(ins);
        assertEquals(EstadoInscripcion.ACTIVADA, ins.getEstado());
    }

    @Test
    void inscribirIndividualComoCompetidor_no_aprobado_lanza_error() {
        InscripcionIndividualDTO dto = new InscripcionIndividualDTO();
        dto.setIdCategoriaTorneo("CAT1");
        dto.setIdRobot("R1");

        Competidor comp = Competidor.builder()
                .idCompetidor("U1")
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .build();

        when(competidorRepo.findByUsuario_IdUsuario("U1")).thenReturn(Optional.of(comp));

        assertThrows(RuntimeException.class, () -> service.inscribirIndividualComoCompetidor("U1", dto));
    }
}
