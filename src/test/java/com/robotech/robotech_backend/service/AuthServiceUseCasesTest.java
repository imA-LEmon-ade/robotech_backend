package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.config.TestMailConfig;
import com.robotech.robotech_backend.dto.LoginResponseDTO;
import com.robotech.robotech_backend.dto.RegistroCompetidorDTO;
import com.robotech.robotech_backend.dto.RegistroJuezDTO;
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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class AuthServiceUseCasesTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private CompetidorRepository competidorRepository;

    @Autowired
    private JuezRepository juezRepository;

    @Autowired
    private CodigoRegistroCompetidorRepository codigoRegistroCompetidorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void login_exitoso_usuario_activo() {
        Usuario admin = Usuario.builder()
                .correo("admin@robotech.test")
                .dni("12345678")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.SUBADMINISTRADOR)))
                .estado(EstadoUsuario.ACTIVO)
                .build();

        usuarioRepository.save(admin);

        LoginResponseDTO response = authService.login("admin@robotech.test", "Secret123!");

        assertNotNull(response.token());
        assertTrue(response.roles().contains(RolUsuario.SUBADMINISTRADOR));
        assertNotNull(response.entidad());
        assertTrue(response.entidad() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> entidad = (Map<String, Object>) response.entidad();
        assertTrue(entidad.containsKey("usuario"));
    }

    @Test
    void login_campos_vacios_lanza_errores_esperados() {
        RuntimeException exAmbos = assertThrows(RuntimeException.class,
                () -> authService.login("", ""));
        assertEquals("Usuario y contraseña vacíos", exAmbos.getMessage());

        RuntimeException exCorreo = assertThrows(RuntimeException.class,
                () -> authService.login("", "abc"));
        assertEquals("Usuario vacío", exCorreo.getMessage());

        RuntimeException exPass = assertThrows(RuntimeException.class,
                () -> authService.login("a@a.com", ""));
        assertEquals("Contraseña vacía", exPass.getMessage());
    }

    @Test
    void login_usuario_no_existe_lanza_error() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("noexiste@robotech.test", "Secret123!"));
        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void login_contrasena_incorrecta_lanza_error() {
        Usuario usuario = Usuario.builder()
                .correo("pass@robotech.test")
                .dni("22334455")
                .contrasenaHash(passwordEncoder.encode("Correcta123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.ADMINISTRADOR)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("pass@robotech.test", "Mala123!"));
        assertEquals("Contraseña incorrecta", ex.getMessage());
    }

    @Test
    void login_cuenta_inactiva_lanza_error() {
        Usuario usuario = Usuario.builder()
                .correo("inactivo@robotech.test")
                .dni("33445566")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.ADMINISTRADOR)))
                .estado(EstadoUsuario.INACTIVO)
                .build();
        usuarioRepository.save(usuario);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("inactivo@robotech.test", "Secret123!"));
        assertEquals("Cuenta inactiva", ex.getMessage());
    }

    @Test
    void login_rol_club_sin_club_lanza_error() {
        Usuario usuario = Usuario.builder()
                .correo("clubsin@robotech.test")
                .dni("44556678")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.CLUB)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("clubsin@robotech.test", "Secret123!"));
        assertEquals("Club no encontrado", ex.getMessage());
    }

    @Test
    void login_rol_club_club_inactivo_lanza_error() {
        Usuario usuario = Usuario.builder()
                .correo("clubinactivo@robotech.test")
                .dni("55667799")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.CLUB)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        Club club = Club.builder()
                .codigoClub("CLB-INACT")
                .nombre("Club Inactivo")
                .correoContacto("clubinactivo@robotech.test")
                .telefonoContacto("987654321")
                .direccionFiscal("Dir 123")
                .estado(EstadoClub.INACTIVO)
                .usuario(usuario)
                .build();
        clubRepository.save(club);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("clubinactivo@robotech.test", "Secret123!"));
        assertEquals("Club inactivo", ex.getMessage());
    }

    @Test
    void login_rol_competidor_sin_competidor_lanza_error() {
        Usuario usuario = Usuario.builder()
                .correo("compsin@robotech.test")
                .dni("66778899")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.COMPETIDOR)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("compsin@robotech.test", "Secret123!"));
        assertEquals("Competidor no encontrado", ex.getMessage());
    }

    @Test
    void login_rol_competidor_no_aprobado_lanza_error() {
        Usuario usuario = Usuario.builder()
                .correo("compnoap@robotech.test")
                .dni("77889900")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.COMPETIDOR)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        Competidor competidor = Competidor.builder()
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .clubActual(null)
                .build();
        competidorRepository.save(competidor);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("compnoap@robotech.test", "Secret123!"));
        assertEquals("Competidor no aprobado", ex.getMessage());
    }

    @Test
    void login_rol_competidor_club_inactivo_y_no_juez_lanza_error() {
        Usuario usuario = Usuario.builder()
                .correo("compcluboff@robotech.test")
                .dni("88990011")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.COMPETIDOR)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        Club club = Club.builder()
                .codigoClub("CLB-OFF01")
                .nombre("Club Off")
                .correoContacto("off@robotech.test")
                .telefonoContacto("987654320")
                .direccionFiscal("Dir 456")
                .estado(EstadoClub.INACTIVO)
                .usuario(usuario)
                .build();
        clubRepository.save(club);

        Competidor competidor = Competidor.builder()
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .clubActual(club)
                .build();
        competidorRepository.save(competidor);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("compcluboff@robotech.test", "Secret123!"));
        assertEquals("Club inactivo", ex.getMessage());
    }

    @Test
    void login_rol_competidor_con_club_inactivo_y_es_juez_permite_login() {
        Usuario usuario = Usuario.builder()
                .correo("compjuez@robotech.test")
                .dni("99001122")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.COMPETIDOR, RolUsuario.JUEZ)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        Club club = Club.builder()
                .codigoClub("CLB-OFF02")
                .nombre("Club Off 2")
                .correoContacto("off2@robotech.test")
                .telefonoContacto("987654325")
                .direccionFiscal("Dir 789")
                .estado(EstadoClub.INACTIVO)
                .usuario(usuario)
                .build();
        clubRepository.save(club);

        Competidor competidor = Competidor.builder()
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .clubActual(club)
                .build();
        competidorRepository.save(competidor);

        Juez juez = Juez.builder()
                .usuario(usuario)
                .licencia("J-001")
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();
        juezRepository.save(juez);

        LoginResponseDTO response = authService.login("compjuez@robotech.test", "Secret123!");
        assertNotNull(response.token());
        assertTrue(response.roles().contains(RolUsuario.COMPETIDOR));
        assertTrue(response.roles().contains(RolUsuario.JUEZ));
        assertTrue(response.entidad() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> entidad = (Map<String, Object>) response.entidad();
        assertTrue(entidad.containsKey("competidor"));
        assertTrue(entidad.containsKey("juez"));
    }

    @Test
    void login_rol_competidor_sin_club_permite_login() {
        Usuario usuario = Usuario.builder()
                .correo("complibre@robotech.test")
                .dni("10111213")
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
        competidorRepository.save(competidor);

        LoginResponseDTO response = authService.login("complibre@robotech.test", "Secret123!");
        assertNotNull(response.token());
        assertTrue(response.roles().contains(RolUsuario.COMPETIDOR));
        assertTrue(response.entidad() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> entidad = (Map<String, Object>) response.entidad();
        assertTrue(entidad.containsKey("competidor"));
    }

    @Test
    void login_rol_juez_sin_juez_lanza_error() {
        Usuario usuario = Usuario.builder()
                .correo("juezsin@robotech.test")
                .dni("12131415")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.JUEZ)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("juezsin@robotech.test", "Secret123!"));
        assertEquals("Juez no encontrado", ex.getMessage());
    }

    @Test
    void login_sin_roles_entrega_usuario_por_defecto() {
        Usuario usuario = Usuario.builder()
                .correo("noroles@robotech.test")
                .dni("13141516")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>())
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        LoginResponseDTO response = authService.login("noroles@robotech.test", "Secret123!");
        assertNotNull(response.token());
        assertTrue(response.entidad() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> entidad = (Map<String, Object>) response.entidad();
        assertTrue(entidad.containsKey("usuario"));
    }

    @Test
    void login_rol_admin_entrega_usuario() {
        Usuario usuario = Usuario.builder()
                .correo("admin2@robotech.test")
                .dni("14151617")
                .contrasenaHash(passwordEncoder.encode("Secret123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.ADMINISTRADOR)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("admin2@robotech.test", "Secret123!"));
        assertEquals("Este inicio de sesion es solo para club, competidor, juez y subadministrador", ex.getMessage());
    }

    @Test
    void registrar_juez_crea_usuario_y_juez() {
        RegistroJuezDTO dto = new RegistroJuezDTO();
        dto.setDni("87654321");
        dto.setCorreo("juez@robotech.test");
        dto.setTelefono("987654321");
        dto.setContrasena("Password123!");
        dto.setLicencia("LIC-001");

        authService.registrarJuez(dto);

        Usuario usuario = usuarioRepository.findByCorreo(dto.getCorreo()).orElseThrow();
        assertEquals(EstadoUsuario.PENDIENTE, usuario.getEstado());
        assertTrue(usuario.getRoles().contains(RolUsuario.JUEZ));
        assertTrue(passwordEncoder.matches(dto.getContrasena(), usuario.getContrasenaHash()));

        Juez juez = juezRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).orElseThrow();
        assertEquals(EstadoValidacion.PENDIENTE, juez.getEstadoValidacion());
        assertEquals("LIC-001", juez.getLicencia());
    }

    @Test
    void registrar_competidor_con_codigo_valido_crea_usuario_y_competidor_y_marca_codigo_usado() {
        Usuario owner = Usuario.builder()
                .correo("club@robotech.test")
                .dni("11223344")
                .telefono("987654322")
                .contrasenaHash(passwordEncoder.encode("Owner123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.CLUB)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(owner);

        Club club = Club.builder()
                .codigoClub("CLB-TEST01")
                .nombre("Club Test")
                .correoContacto("club@robotech.test")
                .telefonoContacto("987654322")
                .direccionFiscal("Av. Test 123")
                .estado(EstadoClub.ACTIVO)
                .usuario(owner)
                .build();
        clubRepository.save(club);

        CodigoRegistroCompetidor codigo = CodigoRegistroCompetidor.builder()
                .codigo("ABCD1234")
                .club(club)
                .expiraEn(new Date(System.currentTimeMillis() + 3600_000))
                .limiteUso(1)
                .usosActuales(0)
                .usado(false)
                .build();
        codigoRegistroCompetidorRepository.save(codigo);

        RegistroCompetidorDTO dto = new RegistroCompetidorDTO();
        dto.setDni("44556677");
        dto.setNombre("Ana");
        dto.setApellido("Perez");
        dto.setCorreo("competidor@robotech.test");
        dto.setTelefono("987654323");
        dto.setContrasena("Comp123!");
        dto.setCodigoClub("ABCD1234");

        authService.registrarCompetidor(dto);

        Usuario usuario = usuarioRepository.findByCorreo(dto.getCorreo()).orElseThrow();
        assertEquals(EstadoUsuario.PENDIENTE, usuario.getEstado());
        assertTrue(usuario.getRoles().contains(RolUsuario.COMPETIDOR));
        assertTrue(passwordEncoder.matches(dto.getContrasena(), usuario.getContrasenaHash()));

        Competidor competidor = competidorRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).orElseThrow();
        assertEquals(EstadoValidacion.PENDIENTE, competidor.getEstadoValidacion());
        assertEquals(club.getIdClub(), competidor.getClubActual().getIdClub());

        CodigoRegistroCompetidor actualizado = codigoRegistroCompetidorRepository.findByCodigo("ABCD1234").orElseThrow();
        assertTrue(actualizado.isUsado());
        assertEquals(1, actualizado.getUsosActuales());
    }

    @Test
    void restablecer_contrasena_solicitar_y_confirmar_actualiza_token_y_contrasena() {
        Usuario usuario = Usuario.builder()
                .correo("reset@robotech.test")
                .dni("55667788")
                .telefono("987654324")
                .contrasenaHash(passwordEncoder.encode("OldPass123!"))
                .roles(new HashSet<>(Set.of(RolUsuario.ADMINISTRADOR)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuarioRepository.save(usuario);

        authService.requestPasswordReset("reset@robotech.test");

        Usuario conToken = usuarioRepository.findByCorreo("reset@robotech.test").orElseThrow();
        assertNotNull(conToken.getPasswordResetToken());
        assertNotNull(conToken.getPasswordResetTokenExpiryDate());
        assertTrue(conToken.getPasswordResetTokenExpiryDate().isAfter(LocalDateTime.now().minusMinutes(1)));

        authService.resetPassword(conToken.getPasswordResetToken(), "NewPass123!");

        Usuario actualizado = usuarioRepository.findByCorreo("reset@robotech.test").orElseThrow();
        assertNull(actualizado.getPasswordResetToken());
        assertNull(actualizado.getPasswordResetTokenExpiryDate());
        assertTrue(passwordEncoder.matches("NewPass123!", actualizado.getContrasenaHash()));
    }
}
