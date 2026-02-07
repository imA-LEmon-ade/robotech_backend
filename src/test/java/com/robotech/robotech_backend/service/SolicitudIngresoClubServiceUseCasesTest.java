package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.config.TestMailConfig;
import com.robotech.robotech_backend.dto.SolicitudIngresoCrearDTO;
import com.robotech.robotech_backend.dto.SolicitudIngresoDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.SolicitudIngresoClub;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class SolicitudIngresoClubServiceUseCasesTest {

    @Autowired private SolicitudIngresoClubService solicitudService;
    @Autowired private SolicitudIngresoClubRepository solicitudRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private CompetidorRepository competidorRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static int dniSeq = 30000000;
    private static int telSeq = 920000000;

    @Test
    void solicitar_exitoso() {
        Club club = crearClub("CLB-SOL01", "Club Sol");
        Competidor competidor = crearCompetidor("sol@robotech.test", "11113333");

        SolicitudIngresoCrearDTO dto = new SolicitudIngresoCrearDTO();
        dto.setCodigoClub(club.getCodigoClub());

        SolicitudIngresoDTO solicitud = solicitudService.solicitar(competidor.getUsuario().getIdUsuario(), dto);

        assertEquals(EstadoSolicitudIngresoClub.PENDIENTE, solicitud.getEstado());
        assertEquals(club.getIdClub(), solicitud.getIdClub());
    }

    @Test
    void solicitar_codigo_invalido_lanza_error() {
        Competidor competidor = crearCompetidor("sol2@robotech.test", "22224444");

        SolicitudIngresoCrearDTO dto = new SolicitudIngresoCrearDTO();
        dto.setCodigoClub("NOEXISTE");

        assertThrows(RuntimeException.class,
                () -> solicitudService.solicitar(competidor.getUsuario().getIdUsuario(), dto));
    }

    @Test
    void aprobar_actualiza_competidor_y_solicitud() {
        Club club = crearClub("CLB-SOL02", "Club Aprob");
        Competidor competidor = crearCompetidor("sol3@robotech.test", "33335555");

        SolicitudIngresoClub solicitud = SolicitudIngresoClub.builder()
                .competidor(competidor)
                .club(club)
                .estado(EstadoSolicitudIngresoClub.PENDIENTE)
                .build();
        solicitud = solicitudRepository.save(solicitud);

        SolicitudIngresoDTO aprobada = solicitudService.aprobar(club.getUsuario().getIdUsuario(), solicitud.getIdSolicitud());

        assertEquals(EstadoSolicitudIngresoClub.APROBADA, aprobada.getEstado());

        Competidor actualizado = competidorRepository.findById(competidor.getIdCompetidor()).orElseThrow();
        assertEquals(club.getIdClub(), actualizado.getClubActual().getIdClub());
        assertEquals(EstadoValidacion.APROBADO, actualizado.getEstadoValidacion());
    }

    @Test
    void cancelar_cambia_estado() {
        Club club = crearClub("CLB-SOL03", "Club Cancel");
        Competidor competidor = crearCompetidor("sol4@robotech.test", "44446666");

        SolicitudIngresoClub solicitud = SolicitudIngresoClub.builder()
                .competidor(competidor)
                .club(club)
                .estado(EstadoSolicitudIngresoClub.PENDIENTE)
                .build();
        solicitud = solicitudRepository.save(solicitud);

        SolicitudIngresoDTO cancelada = solicitudService.cancelar(competidor.getUsuario().getIdUsuario(), solicitud.getIdSolicitud());
        assertEquals(EstadoSolicitudIngresoClub.CANCELADA, cancelada.getEstado());
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
                .clubActual(null)
                .build();
        return competidorRepository.save(competidor);
    }

    private static String nextDni() {
        return String.format("%08d", dniSeq++);
    }

    private static String nextTelefono() {
        return String.valueOf(telSeq++);
    }
}
