package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.config.TestMailConfig;
import com.robotech.robotech_backend.dto.InscripcionIndividualDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class InscripcionTorneoServiceUseCasesTest {

    @Autowired private InscripcionTorneoService inscripcionService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private CompetidorRepository competidorRepository;
    @Autowired private RobotRepository robotRepository;
    @Autowired private TorneoRepository torneoRepository;
    @Autowired private CategoriaTorneoRepository categoriaTorneoRepository;
    @Autowired private InscripcionTorneoRepository inscripcionTorneoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static int dniSeq = 20000000;
    private static int telSeq = 910000000;

    @Test
    void inscribir_individual_como_club_exitoso() {
        Club club = crearClub("CLB-INS01", "Club Ins");
        Competidor competidor = crearCompetidor("comp@robotech.test", "11112223", club, EstadoValidacion.APROBADO);
        Robot robot = crearRobot(competidor, "RoboIns", "nickins", CategoriaCompetencia.MINISUMO);
        CategoriaTorneo categoria = crearCategoriaIndividualAbierta("Torneo Club", 10);

        InscripcionIndividualDTO dto = new InscripcionIndividualDTO();
        dto.setIdCategoriaTorneo(categoria.getIdCategoriaTorneo());
        dto.setIdRobot(robot.getIdRobot());

        InscripcionTorneo inscripcion = inscripcionService.inscribirIndividualComoClub(club.getUsuario().getIdUsuario(), dto);

        assertNotNull(inscripcion.getIdInscripcion());
        assertEquals(EstadoInscripcion.ACTIVADA, inscripcion.getEstado());
    }

    @Test
    void inscribir_individual_como_club_robot_de_otro_club_lanza_error() {
        Club club = crearClub("CLB-INS02", "Club A");
        Club otro = crearClub("CLB-INS03", "Club B");
        Competidor competidor = crearCompetidor("comp2@robotech.test", "22223333", otro, EstadoValidacion.APROBADO);
        Robot robot = crearRobot(competidor, "RoboX", "nickx", CategoriaCompetencia.MINISUMO);
        CategoriaTorneo categoria = crearCategoriaIndividualAbierta("Torneo Club 2", 10);

        InscripcionIndividualDTO dto = new InscripcionIndividualDTO();
        dto.setIdCategoriaTorneo(categoria.getIdCategoriaTorneo());
        dto.setIdRobot(robot.getIdRobot());

        assertThrows(RuntimeException.class,
                () -> inscripcionService.inscribirIndividualComoClub(club.getUsuario().getIdUsuario(), dto));
    }

    @Test
    void inscribir_individual_como_competidor_exitoso() {
        Competidor competidor = crearCompetidor("free@robotech.test", "33334444", null, EstadoValidacion.APROBADO);
        Robot robot = crearRobot(competidor, "RoboFree", "nickfree", CategoriaCompetencia.MINISUMO);
        CategoriaTorneo categoria = crearCategoriaIndividualAbierta("Torneo Free", 10);

        InscripcionIndividualDTO dto = new InscripcionIndividualDTO();
        dto.setIdCategoriaTorneo(categoria.getIdCategoriaTorneo());
        dto.setIdRobot(robot.getIdRobot());

        InscripcionTorneo inscripcion = inscripcionService.inscribirIndividualComoCompetidor(competidor.getUsuario().getIdUsuario(), dto);

        assertNotNull(inscripcion.getIdInscripcion());
        assertEquals(EstadoInscripcion.ACTIVADA, inscripcion.getEstado());
    }

    @Test
    void inscribir_individual_como_competidor_inscripciones_cerradas_lanza_error() {
        Competidor competidor = crearCompetidor("close@robotech.test", "44445555", null, EstadoValidacion.APROBADO);
        Robot robot = crearRobot(competidor, "RoboClose", "nickclose", CategoriaCompetencia.MINISUMO);

        Torneo torneo = crearTorneo("Torneo Cerrado");
        CategoriaTorneo categoria = CategoriaTorneo.builder()
                .torneo(torneo)
                .categoria(CategoriaCompetencia.MINISUMO)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .maxParticipantes(1)
                .inscripcionesCerradas(true)
                .build();
        categoria = categoriaTorneoRepository.save(categoria);

        InscripcionIndividualDTO dto = new InscripcionIndividualDTO();
        dto.setIdCategoriaTorneo(categoria.getIdCategoriaTorneo());
        dto.setIdRobot(robot.getIdRobot());

        assertThrows(RuntimeException.class,
                () -> inscripcionService.inscribirIndividualComoCompetidor(competidor.getUsuario().getIdUsuario(), dto));
    }

    @Test
    void anular_inscripcion_cambia_estado() {
        Competidor competidor = crearCompetidor("anular@robotech.test", "55556666", null, EstadoValidacion.APROBADO);
        Robot robot = crearRobot(competidor, "RoboAnular", "nickanular", CategoriaCompetencia.MINISUMO);
        CategoriaTorneo categoria = crearCategoriaIndividualAbierta("Torneo Anular", 10);

        InscripcionTorneo inscripcion = InscripcionTorneo.builder()
                .categoriaTorneo(categoria)
                .robot(robot)
                .estado(EstadoInscripcion.ACTIVADA)
                .fechaInscripcion(new Date())
                .build();
        inscripcion = inscripcionTorneoRepository.save(inscripcion);

        InscripcionTorneo anulada = inscripcionService.anularInscripcion(inscripcion.getIdInscripcion(), "Motivo");

        assertEquals(EstadoInscripcion.ANULADA, anulada.getEstado());
        assertEquals("Motivo", anulada.getMotivoAnulacion());
    }

    private Club crearClub(String codigo, String nombre) {
        Usuario owner = Usuario.builder()
                .correo(nombre.toLowerCase().replace(" ", "") + "@club.test")
                .dni(nextDni())
                .telefono(nextTelefono())
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.CLUB)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(owner);

        Club club = Club.builder()
                .codigoClub(codigo)
                .nombre(nombre)
                .correoContacto(owner.getCorreo())
                .telefonoContacto(owner.getTelefono())
                .direccionFiscal("Av. Test 123")
                .estado(EstadoClub.ACTIVO)
                .usuario(owner)
                .build();
        return clubRepository.save(club);
    }

    private Competidor crearCompetidor(String correo, String dni, Club club, EstadoValidacion estado) {
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
                .estadoValidacion(estado)
                .clubActual(club)
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

    private Torneo crearTorneo(String nombre) {
        Torneo torneo = Torneo.builder()
                .nombre(nombre)
                .estado("ACTIVO")
                .fechaAperturaInscripcion(new Date(System.currentTimeMillis() - 86_400_000L))
                .fechaCierreInscripcion(new Date(System.currentTimeMillis() + 86_400_000L))
                .build();
        return torneoRepository.save(torneo);
    }

    private CategoriaTorneo crearCategoriaIndividualAbierta(String nombreTorneo, int max) {
        Torneo torneo = crearTorneo(nombreTorneo);
        CategoriaTorneo categoria = CategoriaTorneo.builder()
                .torneo(torneo)
                .categoria(CategoriaCompetencia.MINISUMO)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .maxParticipantes(max)
                .inscripcionesCerradas(false)
                .build();
        return categoriaTorneoRepository.save(categoria);
    }

    private static String nextDni() {
        return String.format("%08d", dniSeq++);
    }

    private static String nextTelefono() {
        return String.valueOf(telSeq++);
    }
}
