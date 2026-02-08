package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CambiarContrasenaDTO;
import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.dto.EditarUsuarioDTO;
import com.robotech.robotech_backend.dto.UsuarioDTO;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DniValidator dniValidator;
    @Mock private TelefonoValidator telefonoValidator;

    @InjectMocks
    private AdminUsuarioService adminUsuarioService;

    private Usuario usuario1;

    @BeforeEach
    void setup() {
        usuario1 = Usuario.builder()
                .idUsuario("U1")
                .dni("12345678")
                .nombres("Ana")
                .apellidos("Perez")
                .correo("ana@robotech.com")
                .telefono("999111222")
                .roles(Set.of(RolUsuario.ADMINISTRADOR))
                .estado(EstadoUsuario.ACTIVO)
                .build();
    }

    @Test
    void listar_sinBusqueda_usaFindAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Usuario> page = new PageImpl<>(List.of(usuario1), pageable, 1);
        when(usuarioRepo.findAll(pageable)).thenReturn(page);

        Page<UsuarioDTO> result = adminUsuarioService.listar(pageable, "");

        assertEquals(1, result.getTotalElements());
        verify(usuarioRepo, times(1)).findAll(pageable);
        verify(usuarioRepo, never()).buscar(anyString(), any(Pageable.class));
    }

    @Test
    void listar_conBusqueda_usaBuscar() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Usuario> page = new PageImpl<>(List.of(usuario1), pageable, 1);
        when(usuarioRepo.buscar("ana", pageable)).thenReturn(page);

        Page<UsuarioDTO> result = adminUsuarioService.listar(pageable, "ana");

        assertEquals(1, result.getTotalElements());
        verify(usuarioRepo, times(1)).buscar("ana", pageable);
        verify(usuarioRepo, never()).findAll(pageable);
    }

    @Test
    void crear_ok() {
        CrearUsuarioDTO dto = new CrearUsuarioDTO(
                "12345678",
                "Ana",
                "Perez",
                "ana@robotech.com",
                "999111222",
                "Pass1!"
        );

        when(usuarioRepo.existsByCorreo(dto.correo())).thenReturn(false);
        when(usuarioRepo.existsByTelefono(dto.telefono())).thenReturn(false);
        when(passwordEncoder.encode(dto.contrasena())).thenReturn("hash");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario created = adminUsuarioService.crear(dto);

        assertEquals("12345678", created.getDni());
        assertEquals(EstadoUsuario.ACTIVO, created.getEstado());
        verify(telefonoValidator, times(1)).validar("999111222");
    }

    @Test
    void crear_duplicate_correo_throws() {
        CrearUsuarioDTO dto = new CrearUsuarioDTO(
                "12345678",
                "Ana",
                "Perez",
                "ana@robotech.com",
                "999111222",
                "Pass1!"
        );

        when(usuarioRepo.existsByCorreo(dto.correo())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> adminUsuarioService.crear(dto));
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void editar_ok_actualiza_campos() {
        EditarUsuarioDTO dto = new EditarUsuarioDTO(
                "Ana",
                "Perez",
                "ana@robotech.com",
                "999111222",
                Set.of(RolUsuario.ADMINISTRADOR),
                EstadoUsuario.ACTIVO
        );

        when(usuarioRepo.findById("U1")).thenReturn(Optional.of(usuario1));
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario updated = adminUsuarioService.editar("U1", dto);

        assertEquals("Ana", updated.getNombres());
        assertEquals("Perez", updated.getApellidos());
        assertEquals("ana@robotech.com", updated.getCorreo());
        assertEquals(EstadoUsuario.ACTIVO, updated.getEstado());
        assertEquals(1, updated.getRoles().size());
    }

    @Test
    void cambiarEstado_ok() {
        when(usuarioRepo.findById("U1")).thenReturn(Optional.of(usuario1));
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario updated = adminUsuarioService.cambiarEstado("U1", "INACTIVO");

        assertEquals(EstadoUsuario.INACTIVO, updated.getEstado());
    }

    @Test
    void cambiarPassword_invalido_lanza_error() {
        assertThrows(RuntimeException.class, () -> adminUsuarioService.cambiarPassword("U1", "short"));
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void cambiarContrasena_actual_incorrecta_lanza_error() {
        CambiarContrasenaDTO dto = new CambiarContrasenaDTO("old", "NewPass1!");
        when(usuarioRepo.findById("U1")).thenReturn(Optional.of(usuario1));
        when(passwordEncoder.matches("old", usuario1.getContrasenaHash())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> adminUsuarioService.cambiarContrasena("U1", dto));
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void eliminar_desactiva_usuario() {
        when(usuarioRepo.findById("U1")).thenReturn(Optional.of(usuario1));

        adminUsuarioService.eliminar("U1");

        assertEquals(EstadoUsuario.INACTIVO, usuario1.getEstado());
        verify(usuarioRepo, times(1)).save(usuario1);
    }
}
