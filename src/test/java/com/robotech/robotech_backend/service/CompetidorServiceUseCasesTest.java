package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.config.TestMailConfig;
import com.robotech.robotech_backend.dto.CompetidorActualizarDTO;
import com.robotech.robotech_backend.dto.CompetidorClubDTO;
import com.robotech.robotech_backend.dto.CompetidorPerfilDTO;
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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class CompetidorServiceUseCasesTest {

    @Autowired private CompetidorService competidorService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private CompetidorRepository competidorRepository;
    @Autowired private RobotRepository robotRepository;
    @Autowired private JuezRepository juezRepository;
    @Autowired private EncuentroRepository encuentroRepository;
    @Autowired private CategoriaTorneoRepository categoriaTorneoRepository;
    @Autowired private TorneoRepository torneoRepository;
    @Autowired private ColiseoRepository coliseoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static int dniSeq = 10000000;
    private static int telSeq = 900000000;

    @Test
    void obtener_perfil_devuelve_datos_y_conteo_robots() {
        Club club = crearClub("CLB-PRF01", "Club Perfil");
        Competidor competidor = crearCompetidor("perfil@robotech.test", "12345678", club, EstadoValidacion.APROBADO);

        crearRobot(competidor, "RoboUno", "nickuno", CategoriaCompetencia.MINISUMO);
        crearRobot(competidor, "RoboDos", "nickdos", CategoriaCompetencia.MICROSUMO);

        CompetidorPerfilDTO dto = competidorService.obtenerPerfil(competidor.getIdCompetidor());

        assertEquals(competidor.getIdCompetidor(), dto.getIdCompetidor());
        assertEquals("Club Perfil", dto.getClubNombre());
        assertEquals(2, dto.getTotalRobots());
        assertEquals("APROBADO", dto.getEstadoValidacion());
    }

    @Test
    void actualizar_perfil_actualiza_datos_basicos() {
        Competidor competidor = crearCompetidor("act@robotech.test", "87654321", null, EstadoValidacion.APROBADO);

        CompetidorActualizarDTO dto = new CompetidorActualizarDTO();
        dto.setNombres("Ana");
        dto.setApellidos("Perez");
        dto.setTelefono("987654321");
        dto.setDni("11223344");
        dto.setCorreo("nuevo@robotech.test");

        competidorService.actualizarPerfil(competidor.getIdCompetidor(), dto);

        Competidor actualizado = competidorRepository.findById(competidor.getIdCompetidor()).orElseThrow();
        Usuario u = actualizado.getUsuario();
        assertEquals("Ana", u.getNombres());
        assertEquals("Perez", u.getApellidos());
        assertEquals("987654321", u.getTelefono());
        assertEquals("11223344", u.getDni());
        assertEquals("nuevo@robotech.test", u.getCorreo());
    }

    @Test
    void actualizar_perfil_correo_en_uso_lanza_error() {
        crearCompetidor("ya@robotech.test", "11112222", null, EstadoValidacion.APROBADO);
        Competidor competidor = crearCompetidor("otro@robotech.test", "33334444", null, EstadoValidacion.APROBADO);

        CompetidorActualizarDTO dto = new CompetidorActualizarDTO();
        dto.setCorreo("ya@robotech.test");

        assertThrows(RuntimeException.class,
                () -> competidorService.actualizarPerfil(competidor.getIdCompetidor(), dto));
    }

    @Test
    void listar_por_club_filtra_por_busqueda() {
        Club club = crearClub("CLB-FLT01", "Club Filtro");
        Competidor c1 = crearCompetidor("ana@robotech.test", "55556666", club, EstadoValidacion.APROBADO);
        Competidor c2 = crearCompetidor("bob@robotech.test", "77778888", club, EstadoValidacion.APROBADO);

        c1.getUsuario().setNombres("Ana");
        c1.getUsuario().setApellidos("Gomez");
        c2.getUsuario().setNombres("Carlos");
        c2.getUsuario().setApellidos("Perez");
        usuarioRepository.save(c1.getUsuario());
        usuarioRepository.save(c2.getUsuario());

        List<CompetidorClubDTO> filtrados = competidorService.listarPorClub(club.getIdClub(), "ana");
        assertEquals(1, filtrados.size());
        assertEquals(c1.getIdCompetidor(), filtrados.get(0).idCompetidor());
    }

    @Test
    void aprobar_competidor_activa_competidor_y_usuario() {
        Competidor competidor = crearCompetidor("aprobar@robotech.test", "99990000", null, EstadoValidacion.PENDIENTE);
        Usuario u = competidor.getUsuario();
        u.setEstado(EstadoUsuario.PENDIENTE);
        u.setRoles(new HashSet<>(Set.of(RolUsuario.COMPETIDOR)));
        usuarioRepository.save(u);

        competidorService.aprobarCompetidor(competidor.getIdCompetidor());

        Competidor actualizado = competidorRepository.findById(competidor.getIdCompetidor()).orElseThrow();
        assertEquals(EstadoValidacion.APROBADO, actualizado.getEstadoValidacion());

        Usuario usuarioActualizado = usuarioRepository.findById(u.getIdUsuario()).orElseThrow();
        assertEquals(EstadoUsuario.ACTIVO, usuarioActualizado.getEstado());
    }

    @Test
    void aprobar_competidor_juez_con_pendientes_lanza_error() {
        Competidor competidor = crearCompetidor("juezpend@robotech.test", "12121212", null, EstadoValidacion.PENDIENTE);
        Usuario u = competidor.getUsuario();
        u.setRoles(new HashSet<>(Set.of(RolUsuario.COMPETIDOR, RolUsuario.JUEZ)));
        usuarioRepository.save(u);

        Juez juez = Juez.builder()
                .usuario(u)
                .licencia("LIC-999")
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();
        juezRepository.save(juez);

        Torneo torneo = crearTorneo("Torneo Pendiente");
        CategoriaTorneo categoria = crearCategoriaTorneo(torneo, CategoriaCompetencia.MINISUMO);
        Coliseo coliseo = crearColiseo("COL-01");

        Encuentro encuentro = Encuentro.builder()
                .idEncuentro("ENCU0001")
                .categoriaTorneo(categoria)
                .juez(juez)
                .coliseo(coliseo)
                .estado(EstadoEncuentro.EN_CURSO)
                .ronda(1)
                .fecha(new Date())
                .tipo(TipoEncuentro.ELIMINACION_DIRECTA)
                .build();
        encuentroRepository.save(encuentro);

        assertThrows(RuntimeException.class,
                () -> competidorService.aprobarCompetidor(competidor.getIdCompetidor()));
    }

    @Test
    void rechazar_competidor_cambia_estado() {
        Competidor competidor = crearCompetidor("rech@robotech.test", "34343434", null, EstadoValidacion.PENDIENTE);

        competidorService.rechazarCompetidor(competidor.getIdCompetidor());

        Competidor actualizado = competidorRepository.findById(competidor.getIdCompetidor()).orElseThrow();
        assertEquals(EstadoValidacion.RECHAZADO, actualizado.getEstadoValidacion());
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
                .estado(EstadoUsuario.PENDIENTE)
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
                .build();
        return torneoRepository.save(torneo);
    }

    private CategoriaTorneo crearCategoriaTorneo(Torneo torneo, CategoriaCompetencia categoria) {
        CategoriaTorneo ct = CategoriaTorneo.builder()
                .torneo(torneo)
                .categoria(categoria)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .maxParticipantes(16)
                .inscripcionesCerradas(false)
                .build();
        return categoriaTorneoRepository.save(ct);
    }

    private Coliseo crearColiseo(String id) {
        Coliseo coliseo = new Coliseo();
        coliseo.setIdColiseo(id);
        coliseo.setNombre("Coliseo " + id);
        coliseo.setUbicacion("Lima");
        return coliseoRepository.save(coliseo);
    }

    private static String nextDni() {
        return String.format("%08d", dniSeq++);
    }

    private static String nextTelefono() {
        return String.valueOf(telSeq++);
    }
}
