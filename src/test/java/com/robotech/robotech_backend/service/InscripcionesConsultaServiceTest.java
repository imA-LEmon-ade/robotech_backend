package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.InscripcionResumenDTO;
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
class InscripcionesConsultaServiceTest {

    @Mock private InscripcionTorneoRepository inscripcionRepo;
    @Mock private EquipoTorneoRepository equipoRepo;

    @InjectMocks
    private InscripcionesConsultaService service;

    @Test
    void listarInscripcionesClub_filtra_por_estado() {
        Torneo torneo = Torneo.builder().nombre("Torneo").fechaInicio(new Date()).build();
        CategoriaTorneo cat = CategoriaTorneo.builder().torneo(torneo).categoria(CategoriaCompetencia.MINISUMO).modalidad(ModalidadCategoria.INDIVIDUAL).build();
        InscripcionTorneo ins = InscripcionTorneo.builder()
                .categoriaTorneo(cat)
                .robot(Robot.builder().nombre("R1").build())
                .estado(EstadoInscripcion.ACTIVADA)
                .fechaInscripcion(new Date())
                .build();

        when(inscripcionRepo.findByRobotCompetidorClubActualUsuarioIdUsuario("C1")).thenReturn(List.of(ins));
        when(equipoRepo.findByClubUsuarioIdUsuario("C1")).thenReturn(List.of());

        List<InscripcionResumenDTO> result = service.listarInscripcionesClub("C1", null, "ACTIVADA");

        assertEquals(1, result.size());
        assertEquals("ACTIVADA", result.get(0).getEstado());
    }

    @Test
    void listarInscripcionesCompetidor_combina_listas() {
        Torneo torneo = Torneo.builder().nombre("Torneo").fechaInicio(new Date()).build();
        CategoriaTorneo cat = CategoriaTorneo.builder().torneo(torneo).categoria(CategoriaCompetencia.MINISUMO).modalidad(ModalidadCategoria.INDIVIDUAL).build();
        InscripcionTorneo ins = InscripcionTorneo.builder()
                .categoriaTorneo(cat)
                .robot(Robot.builder().nombre("R1").build())
                .estado(EstadoInscripcion.ACTIVADA)
                .fechaInscripcion(new Date())
                .build();

        EquipoTorneo eq = EquipoTorneo.builder()
                .categoriaTorneo(cat)
                .robots(List.of(Robot.builder().nombre("R2").build()))
                .estado(EstadoEquipoTorneo.ACTIVADA)
                .build();

        when(inscripcionRepo.findByRobotCompetidorUsuarioIdUsuario("U1")).thenReturn(List.of(ins));
        when(equipoRepo.findByRobotsCompetidorUsuarioIdUsuario("U1")).thenReturn(List.of(eq));

        List<InscripcionResumenDTO> result = service.listarInscripcionesCompetidor("U1");

        assertEquals(2, result.size());
    }
}
