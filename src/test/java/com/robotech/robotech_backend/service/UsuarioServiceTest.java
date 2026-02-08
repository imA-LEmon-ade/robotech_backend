package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearAdminDTO;
import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.CodigoRegistroCompetidor;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.repository.CodigoRegistroCompetidorRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.NicknameValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CompetidorRepository competidorRepository;
    @Mock private CodigoRegistroCompetidorRepository codigoRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NicknameValidator nicknameValidator;
    @Mock private DniValidator dniValidator;
    @Mock private TelefonoValidator telefonoValidator;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void crearUsuario_ok_actualiza_codigo_y_crea_competidor() {
        CrearUsuarioDTO dto = new CrearUsuarioDTO(
                "12345678",
                "Ana",
                "Perez",
                "ana@robotech.com",
                "999111222",
                "Pass1!"
        );

        Club club = Club.builder().idClub("C1").build();
        CodigoRegistroCompetidor codigo = CodigoRegistroCompetidor.builder()
                .codigo("ABC12345")
                .club(club)
                .expiraEn(new Date(System.currentTimeMillis() + 60000))
                .limiteUso(1)
                .usosActuales(0)
                .usado(false)
                .build();

        when(usuarioRepository.existsByCorreo("ana@robotech.com")).thenReturn(false);
        when(usuarioRepository.existsByTelefono("999111222")).thenReturn(false);
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);
        when(codigoRepo.findByCodigo("ABC12345")).thenReturn(Optional.of(codigo));
        when(passwordEncoder.encode("Pass1!")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario created = usuarioService.crearUsuario(dto, "ABC12345");

        assertEquals(EstadoUsuario.ACTIVO, created.getEstado());
        assertEquals(true, created.getRoles().contains(RolUsuario.COMPETIDOR));
        verify(competidorRepository, times(1)).save(any(Competidor.class));
        verify(codigoRepo, times(1)).save(codigo);
    }

    @Test
    void crearUsuario_codigo_expirado_lanza_error() {
        CrearUsuarioDTO dto = new CrearUsuarioDTO(
                "12345678",
                "Ana",
                "Perez",
                "ana@robotech.com",
                "999111222",
                "Pass1!"
        );

        CodigoRegistroCompetidor codigo = CodigoRegistroCompetidor.builder()
                .codigo("ABC12345")
                .expiraEn(new Date(System.currentTimeMillis() - 60000))
                .limiteUso(1)
                .usosActuales(0)
                .usado(false)
                .build();

        when(usuarioRepository.existsByCorreo("ana@robotech.com")).thenReturn(false);
        when(usuarioRepository.existsByTelefono("999111222")).thenReturn(false);
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);
        when(codigoRepo.findByCodigo("ABC12345")).thenReturn(Optional.of(codigo));

        assertThrows(RuntimeException.class, () -> usuarioService.crearUsuario(dto, "ABC12345"));
    }

    @Test
    void login_ok_devuelve_usuario() {
        Usuario usuario = Usuario.builder()
                .correo("ana@robotech.com")
                .contrasenaHash("hash")
                .estado(EstadoUsuario.ACTIVO)
                .build();

        when(usuarioRepository.findByCorreo("ana@robotech.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Pass1!", "hash")).thenReturn(true);

        assertEquals(true, usuarioService.login("ana@robotech.com", "Pass1!").isPresent());
    }

    @Test
    void eliminarUsuario_cambia_estado() {
        Usuario usuario = Usuario.builder().idUsuario("U1").estado(EstadoUsuario.ACTIVO).build();
        when(usuarioRepository.findById("U1")).thenReturn(Optional.of(usuario));

        usuarioService.eliminarUsuario("U1");

        assertEquals(EstadoUsuario.INACTIVO, usuario.getEstado());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void crearAdministrador_ok() {
        CrearAdminDTO dto = new CrearAdminDTO("12345678", "Ana", "Perez", "ana@robotech.com", "999111222", "Pass1!");

        when(usuarioRepository.existsByCorreo("ana@robotech.com")).thenReturn(false);
        when(usuarioRepository.existsByTelefono("999111222")).thenReturn(false);
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);
        when(passwordEncoder.encode("Pass1!")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario created = usuarioService.crearAdministrador(dto);

        assertEquals(true, created.getRoles().contains(RolUsuario.ADMINISTRADOR));
    }
}
