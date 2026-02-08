package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.*;
import com.robotech.robotech_backend.exception.InvalidPasswordResetTokenException;
import com.robotech.robotech_backend.exception.UserNotFoundException;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.JuezRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.security.JwtService;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepo;
    @Mock private ClubRepository clubRepo;
    @Mock private CompetidorRepository competidorRepo;
    @Mock private JuezRepository juezRepo;
    @Mock private CodigoRegistroService codigoService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private TokenGeneratorService tokenGeneratorService;
    @Mock private EmailService emailService;
    @Mock private DniValidator dniValidator;
    @Mock private TelefonoValidator telefonoValidator;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_competidor_ok() {
        Usuario usuario = Usuario.builder()
                .idUsuario("U1")
                .correo("ana@robotech.com")
                .contrasenaHash("hash")
                .estado(EstadoUsuario.ACTIVO)
                .roles(Set.of(RolUsuario.COMPETIDOR))
                .nombres("Ana")
                .apellidos("Perez")
                .build();
        Competidor comp = Competidor.builder()
                .idCompetidor("U1")
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .clubActual(null)
                .build();

        when(usuarioRepo.findByCorreo("ana@robotech.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Pass1!", "hash")).thenReturn(true);
        when(jwtService.generarToken(usuario)).thenReturn("token123");
        when(competidorRepo.findByUsuario_IdUsuario("U1")).thenReturn(Optional.of(comp));

        LoginResponseDTO resp = authService.login("ana@robotech.com", "Pass1!");

        assertEquals("token123", resp.token());
        assertEquals(true, resp.roles().contains(RolUsuario.COMPETIDOR));
        @SuppressWarnings("unchecked")
        Map<String, Object> entidad = (Map<String, Object>) resp.entidad();
        assertEquals(true, entidad.containsKey("competidor"));
    }

    @Test
    void login_usuario_no_encontrado_lanza_error() {
        when(usuarioRepo.findByCorreo("x@x.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.login("x@x.com", "Pass1!"));
    }

    @Test
    void requestPasswordReset_ok_envia_email() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("test@robotech.com");

        when(usuarioRepo.findByCorreo("test@robotech.com")).thenReturn(Optional.of(usuario));
        when(tokenGeneratorService.generateSecureToken()).thenReturn("tok");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.requestPasswordReset("test@robotech.com");

        assertEquals("tok", usuario.getPasswordResetToken());
        verify(emailService, times(1)).sendPasswordResetEmail("test@robotech.com", "tok");
    }

    @Test
    void requestPasswordReset_usuario_no_encontrado() {
        when(usuarioRepo.findByCorreo("x@x.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> authService.requestPasswordReset("x@x.com"));
    }

    @Test
    void resetPassword_expirado_lanza_error() {
        Usuario usuario = new Usuario();
        usuario.setPasswordResetToken("tok");
        usuario.setPasswordResetTokenExpiryDate(LocalDateTime.now().minusMinutes(1));

        when(usuarioRepo.findByPasswordResetToken("tok")).thenReturn(Optional.of(usuario));

        assertThrows(InvalidPasswordResetTokenException.class, () -> authService.resetPassword("tok", "NewPass1!"));
    }

    @Test
    void resetPassword_ok_actualiza_hash_y_limpia_token() {
        Usuario usuario = new Usuario();
        usuario.setPasswordResetToken("tok");
        usuario.setPasswordResetTokenExpiryDate(LocalDateTime.now().plusMinutes(5));

        when(usuarioRepo.findByPasswordResetToken("tok")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("NewPass1!")).thenReturn("hash2");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword("tok", "NewPass1!");

        assertEquals("hash2", usuario.getContrasenaHash());
        assertEquals(null, usuario.getPasswordResetToken());
    }

    @Test
    void registrarCompetidor_ok() {
        RegistroCompetidorDTO dto = new RegistroCompetidorDTO();
        dto.setDni("12345678");
        dto.setNombre("Ana");
        dto.setApellido("Perez");
        dto.setCorreo("ana@robotech.com");
        dto.setTelefono("999111222");
        dto.setContrasena("Pass1!");
        dto.setCodigoClub("CLUB01");

        Club club = Club.builder().idClub("C1").build();
        CodigoRegistroCompetidor codigo = CodigoRegistroCompetidor.builder().club(club).build();

        when(usuarioRepo.existsByCorreo("ana@robotech.com")).thenReturn(false);
        when(usuarioRepo.existsByTelefono("999111222")).thenReturn(false);
        when(codigoService.validarCodigo("CLUB01")).thenReturn(codigo);
        when(passwordEncoder.encode("Pass1!")).thenReturn("hash");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.registrarCompetidor(dto);

        verify(competidorRepo, times(1)).save(any(Competidor.class));
        verify(codigoService, times(1)).marcarUso(codigo);
    }

    @Test
    void registrarClub_ok_genera_codigo() {
        RegistroClubDTO dto = new RegistroClubDTO();
        dto.setNombre("Club A");
        dto.setCorreo("club@robotech.com");
        dto.setTelefono("999111222");
        dto.setDireccionFiscal("Av 123");
        dto.setContrasena("Pass1!");

        when(usuarioRepo.existsByCorreo("club@robotech.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass1!")).thenReturn("hash");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario("ABCDEF12");
            return u;
        });
        when(clubRepo.save(any(Club.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.registrarClub(dto);

        ArgumentCaptor<Club> clubCaptor = ArgumentCaptor.forClass(Club.class);
        verify(clubRepo, times(1)).save(clubCaptor.capture());
        assertEquals("CLB-ABCDEF", clubCaptor.getValue().getCodigoClub());
    }

    @Test
    void registrarJuez_ok() {
        RegistroJuezDTO dto = new RegistroJuezDTO();
        dto.setDni("12345678");
        dto.setCorreo("juez@robotech.com");
        dto.setTelefono("999111222");
        dto.setContrasena("Pass1!");
        dto.setLicencia("LIC-01");

        when(usuarioRepo.existsByCorreo("juez@robotech.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass1!")).thenReturn("hash");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.registrarJuez(dto);

        verify(juezRepo, times(1)).save(any(Juez.class));
    }
}
