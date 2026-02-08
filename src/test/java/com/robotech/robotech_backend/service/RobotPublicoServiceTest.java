package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RobotPublicoDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Robot;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.CategoriaCompetencia;
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
class RobotPublicoServiceTest {

    @Mock private RobotRepository robotRepo;

    @InjectMocks
    private RobotPublicoService robotPublicoService;

    @Test
    void obtenerRobotsPublicos_mapea_independiente() {
        Robot r = Robot.builder()
                .idRobot("R1")
                .nombre("Titan")
                .categoria(CategoriaCompetencia.MINISUMO)
                .nickname("TTN")
                .competidor(null)
                .build();

        when(robotRepo.findAllWithDetalles()).thenReturn(List.of(r));

        List<RobotPublicoDTO> result = robotPublicoService.obtenerRobotsPublicos();

        assertEquals(1, result.size());
        assertEquals("Independiente", result.get(0).getNombreClub());
    }

    @Test
    void obtenerRobotsPublicos_mapea_competidor_y_club() {
        Usuario u = Usuario.builder().nombres("Ana").apellidos("Perez").build();
        Club c = Club.builder().nombre("Club A").build();
        Competidor comp = Competidor.builder().usuario(u).clubActual(c).build();
        Robot r = Robot.builder()
                .idRobot("R1")
                .nombre("Titan")
                .categoria(CategoriaCompetencia.MINISUMO)
                .nickname("TTN")
                .competidor(comp)
                .build();

        when(robotRepo.findAllWithDetalles()).thenReturn(List.of(r));

        List<RobotPublicoDTO> result = robotPublicoService.obtenerRobotsPublicos();

        assertEquals("Ana Perez", result.get(0).getNombreDueño());
        assertEquals("Club A", result.get(0).getNombreClub());
    }
}
