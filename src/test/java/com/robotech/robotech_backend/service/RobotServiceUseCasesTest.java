package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.config.TestMailConfig;
import com.robotech.robotech_backend.dto.RobotDTO;
import com.robotech.robotech_backend.dto.RobotResponseDTO;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Robot;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.CategoriaCompetencia;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.model.enums.EstadoRobot;
import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class RobotServiceUseCasesTest {

    @Autowired private RobotService robotService;
    @Autowired private RobotRepository robotRepository;
    @Autowired private CompetidorRepository competidorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void crear_robot_exitoso() {
        Competidor competidor = crearCompetidor("robotok@robotech.test", "10101010");

        RobotDTO dto = new RobotDTO("RoboOne", "MINISUMO", "nick1", null);
        RobotResponseDTO response = robotService.crearRobot(competidor.getIdCompetidor(), dto);

        assertNotNull(response.idRobot());
        assertEquals("RoboOne", response.nombre());
        assertEquals("MINISUMO", response.categoria());
        assertEquals("nick1", response.nickname());
    }

    @Test
    void crear_robot_categoria_duplicada_lanza_conflicto() {
        Competidor competidor = crearCompetidor("robotdup@robotech.test", "20202020");
        crearRobot(competidor, "RoboA", "nickA", CategoriaCompetencia.MINISUMO);

        RobotDTO dto = new RobotDTO("RoboB", "MINISUMO", "nickB", null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> robotService.crearRobot(competidor.getIdCompetidor(), dto));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void crear_robot_nickname_duplicado_lanza_conflicto() {
        Competidor competidor = crearCompetidor("robotnick@robotech.test", "30303030");
        crearRobot(competidor, "RoboA", "nickA", CategoriaCompetencia.MINISUMO);

        RobotDTO dto = new RobotDTO("RoboB", "MICROSUMO", "nickA", null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> robotService.crearRobot(competidor.getIdCompetidor(), dto));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void editar_robot_cambiar_categoria_con_duplicado_lanza_conflicto() {
        Competidor competidor = crearCompetidor("robotedit@robotech.test", "40404040");
        Robot r1 = crearRobot(competidor, "RoboA", "nickA", CategoriaCompetencia.MINISUMO);
        crearRobot(competidor, "RoboB", "nickB", CategoriaCompetencia.MICROSUMO);

        RobotDTO dto = new RobotDTO("RoboA", "MICROSUMO", "nickA", r1.getIdRobot());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> robotService.editarRobot(r1.getIdRobot(), dto));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void editar_robot_nickname_duplicado_lanza_conflicto() {
        Competidor competidor = crearCompetidor("robotedit2@robotech.test", "50505050");
        Robot r1 = crearRobot(competidor, "RoboA", "nickA", CategoriaCompetencia.MINISUMO);
        crearRobot(competidor, "RoboB", "nickB", CategoriaCompetencia.MICROSUMO);

        RobotDTO dto = new RobotDTO("RoboA", "MINISUMO", "nickB", r1.getIdRobot());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> robotService.editarRobot(r1.getIdRobot(), dto));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void eliminar_robot_no_existe_lanza_no_encontrado() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> robotService.eliminar("NOEXISTE"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void listar_por_competidor_devuelve_robots() {
        Competidor competidor = crearCompetidor("robotlist@robotech.test", "60606060");
        crearRobot(competidor, "RoboA", "nickA", CategoriaCompetencia.MINISUMO);
        crearRobot(competidor, "RoboB", "nickB", CategoriaCompetencia.MICROSUMO);

        assertEquals(2, robotService.listarPorCompetidor(competidor.getIdCompetidor()).size());
    }

    private Competidor crearCompetidor(String correo, String dni) {
        Usuario usuario = Usuario.builder()
                .correo(correo)
                .dni(dni)
                .telefono("9876543" + dni.substring(0, 2))
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.COMPETIDOR)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        Competidor competidor = Competidor.builder()
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();
        return competidorRepository.save(competidor);
    }

    private Robot crearRobot(Competidor competidor, String nombre, String nickname, CategoriaCompetencia categoria) {
        Robot robot = Robot.builder()
                .nombre(nombre)
                .nickname(nickname)
                .categoria(categoria)
                .estado(EstadoRobot.ACTIVO)
                .competidor(competidor)
                .build();
        return robotRepository.save(robot);
    }
}
