package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RobotDTO;
import com.robotech.robotech_backend.dto.RobotResponseDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Robot;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.CategoriaCompetencia;
import com.robotech.robotech_backend.model.enums.EstadoRobot;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import com.robotech.robotech_backend.service.validadores.NicknameValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RobotServiceTest {

    @Mock private RobotRepository robotRepo;
    @Mock private CompetidorRepository competidorRepo;
    @Mock private NicknameValidator nicknameValidator;

    @InjectMocks
    private RobotService robotService;

    @Test
    void crearRobot_ok() {
        RobotDTO dto = new RobotDTO("Titan", "MINISUMO", "TTN", null);
        Usuario usuario = Usuario.builder().dni("12345678").build();
        Competidor comp = Competidor.builder().idCompetidor("C1").usuario(usuario).build();

        when(competidorRepo.findById("C1")).thenReturn(Optional.of(comp));
        when(robotRepo.existsByCompetidor_IdCompetidorAndCategoria("C1", CategoriaCompetencia.MINISUMO)).thenReturn(false);
        when(robotRepo.existsByNickname("TTN")).thenReturn(false);
        when(robotRepo.existsByNombre("Titan")).thenReturn(false);
        when(robotRepo.save(any(Robot.class))).thenAnswer(inv -> {
            Robot r = inv.getArgument(0);
            r.setIdRobot("R1");
            r.setEstado(EstadoRobot.ACTIVO);
            return r;
        });

        RobotResponseDTO resp = robotService.crearRobot("C1", dto);

        assertEquals("R1", resp.idRobot());
        assertEquals("Titan", resp.nombre());
        assertEquals("MINISUMO", resp.categoria());
    }

    @Test
    void crearRobot_categoria_duplicada_lanza_conflict() {
        RobotDTO dto = new RobotDTO("Titan", "MINISUMO", "TTN", null);
        Competidor comp = Competidor.builder().idCompetidor("C1").usuario(new Usuario()).build();

        when(competidorRepo.findById("C1")).thenReturn(Optional.of(comp));
        when(robotRepo.existsByCompetidor_IdCompetidorAndCategoria("C1", CategoriaCompetencia.MINISUMO)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> robotService.crearRobot("C1", dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(robotRepo, never()).save(any(Robot.class));
    }

    @Test
    void editarRobot_nickname_ocupado_lanza_conflict() {
        RobotDTO dto = new RobotDTO("Titan", "MINISUMO", "NEW", null);
        Robot robot = Robot.builder()
                .idRobot("R1")
                .nombre("Old")
                .nickname("OLD")
                .categoria(CategoriaCompetencia.MINISUMO)
                .competidor(Competidor.builder().idCompetidor("C1").build())
                .build();

        when(robotRepo.findById("R1")).thenReturn(Optional.of(robot));
        when(robotRepo.existsByNickname("NEW")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> robotService.editarRobot("R1", dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void eliminar_no_existe_lanza_not_found() {
        when(robotRepo.existsById("R1")).thenReturn(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> robotService.eliminar("R1"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
