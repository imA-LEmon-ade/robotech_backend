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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
        PageRequest pageable = PageRequest.of(0, 10);
        Usuario u1 = Usuario.builder().nombres("Ana").apellidos("Perez").build();
        Club c1 = Club.builder().nombre("Club A").build();
        Competidor comp1 = Competidor.builder().idCompetidor("C1").usuario(u1).clubActual(c1).build();

        Usuario u2 = Usuario.builder().nombres("Luis").apellidos("Gomez").build();
        Club c2 = Club.builder().nombre("Club B").build();
        Competidor comp2 = Competidor.builder().idCompetidor("C2").usuario(u2).clubActual(c2).build();

        Page<Competidor> page = new PageImpl<>(List.of(comp1, comp2), pageable, 2);
        when(competidorRepo.buscarPublico(null, pageable)).thenReturn(page);
        when(robotRepo.findByCompetidor_IdCompetidorIn(List.of("C1", "C2"))).thenReturn(List.of(
                Robot.builder().nombre("R1").competidor(comp1).build(),
                Robot.builder().nombre("R2").competidor(comp1).build(),
                Robot.builder().nombre("R3").competidor(comp2).build()
        ));

        var result = competidorPublicoService.obtenerRanking(pageable, null);

        assertEquals(2, result.content().size());
        assertEquals(1, result.content().get(0).getRanking());
        assertEquals(2, result.content().get(1).getRanking());
        assertEquals(2, result.content().get(0).getNombresRobots().size());
    }
}
