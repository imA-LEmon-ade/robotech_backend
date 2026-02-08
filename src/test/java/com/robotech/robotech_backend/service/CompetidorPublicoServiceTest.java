package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CompetidorPublicoDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Robot;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetidorPublicoServiceTest {

    @Mock private CompetidorRepository competidorRepo;
    @Mock private RobotRepository robotRepo;

    @InjectMocks
    private CompetidorPublicoService competidorPublicoService;

    @Test
    void obtenerRanking_mapea_robots_y_asigna_ranking() {
        Usuario u1 = Usuario.builder().nombres("Ana").apellidos("Perez").build();
        Club c1 = Club.builder().nombre("Club A").build();
        Competidor comp1 = Competidor.builder().idCompetidor("C1").usuario(u1).clubActual(c1).build();

        Usuario u2 = Usuario.builder().nombres("Luis").apellidos("Gomez").build();
        Club c2 = Club.builder().nombre("Club B").build();
        Competidor comp2 = Competidor.builder().idCompetidor("C2").usuario(u2).clubActual(c2).build();

        when(competidorRepo.findAll()).thenReturn(List.of(comp1, comp2));
        when(robotRepo.findByCompetidor_IdCompetidor("C1")).thenReturn(List.of(
                Robot.builder().nombre("R1").build(),
                Robot.builder().nombre("R2").build()
        ));
        when(robotRepo.findByCompetidor_IdCompetidor("C2")).thenReturn(List.of(
                Robot.builder().nombre("R3").build()
        ));

        List<CompetidorPublicoDTO> result = competidorPublicoService.obtenerRanking();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getRanking());
        assertEquals(2, result.get(1).getRanking());
        assertEquals(2, result.get(0).getNombresRobots().size());
    }
}
