package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RobotAdminDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRobotServiceTest {

    @Mock private RobotRepository robotRepo;

    @InjectMocks
    private AdminRobotService adminRobotService;

    @Test
    void listarRobots_mapea_campos() {
        Usuario usuario = Usuario.builder().dni("12345678").build();
        Club club = Club.builder().idClub("C1").nombre("Club A").build();
        Competidor competidor = Competidor.builder().usuario(usuario).clubActual(club).build();
        Robot robot = Robot.builder()
                .idRobot("R1")
                .nombre("Titan")
                .nickname("TTN")
                .categoria(CategoriaCompetencia.MINISUMO)
                .competidor(competidor)
                .build();

        PageRequest pageable = PageRequest.of(0, 20);
        Page<Robot> page = new PageImpl<>(List.of(robot), pageable, 1);
        when(robotRepo.filtrarRobotsPage("ti", CategoriaCompetencia.MINISUMO, "C1", pageable))
                .thenReturn(page);

        Page<RobotAdminDTO> result = adminRobotService.listarRobots("ti", "MINISUMO", "C1", pageable);

        RobotAdminDTO dto = result.getContent().get(0);
        assertEquals("R1", dto.getIdRobot());
        assertEquals("Titan", dto.getNombre());
        assertEquals("TTN", dto.getNickname());
        assertEquals("MINISUMO", dto.getCategoria());
        assertEquals("12345678", dto.getCompetidor());
        assertEquals("Club A", dto.getClub());
    }

    @Test
    void listarRobots_sin_competidor_no_revienta() {
        Robot robot = Robot.builder()
                .idRobot("R2")
                .nombre("Solo")
                .nickname("SOLO")
                .categoria(CategoriaCompetencia.MINISUMO)
                .competidor(null)
                .build();

        PageRequest pageable = PageRequest.of(0, 20);
        Page<Robot> page = new PageImpl<>(List.of(robot), pageable, 1);
        when(robotRepo.filtrarRobotsPage(null, null, null, pageable)).thenReturn(page);

        Page<RobotAdminDTO> result = adminRobotService.listarRobots(null, null, null, pageable);

        RobotAdminDTO dto = result.getContent().get(0);
        assertEquals("R2", dto.getIdRobot());
        assertEquals(null, dto.getCompetidor());
        assertEquals(null, dto.getClub());
    }
}
