package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.JuezAdminDTO;
import com.robotech.robotech_backend.dto.JuezDTO;
import com.robotech.robotech_backend.dto.JuezSelectDTO;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Juez;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoEncuentro;
import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.EncuentroRepository;
import com.robotech.robotech_backend.repository.JuezRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
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

import java.util.HashSet;
import java.util.List;
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
class AdminJuezServiceTest {

    @Mock private JuezRepository juezRepository;
    @Mock private CompetidorRepository competidorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EncuentroRepository encuentroRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DniValidator dniValidator;
    @Mock private TelefonoValidator telefonoValidator;

    @InjectMocks
    private AdminJuezService adminJuezService;

    @Test
    void listar_maps_usuario() {
        Usuario usuario = Usuario.builder()
                .idUsuario("U1")
                .dni("12345678")
                .nombres("Ana")
                .apellidos("Perez")
                .correo("ana@robotech.com")
                .roles(Set.of(RolUsuario.JUEZ))
                .estado(EstadoUsuario.ACTIVO)
                .telefono("999111222")
                .build();
        Juez juez = Juez.builder()
                .idJuez("J1")
                .licencia("LIC-01")
                .estadoValidacion(EstadoValidacion.APROBADO)
                .usuario(usuario)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        Page<Juez> page = new PageImpl<>(List.of(juez), pageable, 1);
        when(juezRepository.buscar("ana", pageable)).thenReturn(page);

        Page<JuezAdminDTO> result = adminJuezService.listar(pageable, "ana");

        assertEquals(1, result.getTotalElements());
        assertEquals("J1", result.getContent().get(0).getIdJuez());
        assertEquals("Ana", result.getContent().get(0).getUsuario().nombres());
    }

    @Test
    void crear_crea_competidor_si_no_existe() {
        JuezDTO dto = JuezDTO.builder()
                .dni("12345678")
                .nombres("Ana")
                .apellidos("Perez")
                .correo("ana@robotech.com")
                .telefono("999111222")
                .contrasena("Pass1!")
                .licencia("LIC-01")
                .creadoPor("ADMIN")
                .build();

        when(passwordEncoder.encode("Pass1!")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario("U1");
            return u;
        });
        when(competidorRepository.findByUsuario_IdUsuario("U1")).thenReturn(Optional.empty());
        when(juezRepository.save(any(Juez.class))).thenAnswer(inv -> inv.getArgument(0));

        Juez created = adminJuezService.crear(dto);

        assertNotNull(created);
        verify(competidorRepository, times(1)).save(any(Competidor.class));
        verify(juezRepository, times(1)).save(any(Juez.class));
    }

    @Test
    void listarJuecesParaSelect_devuelve_nombre_completo() {
        Usuario usuario = Usuario.builder()
                .nombres("Ana")
                .apellidos("Perez")
                .build();
        Juez juez = Juez.builder()
                .idJuez("J1")
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();

        when(juezRepository.findByEstadoValidacion(EstadoValidacion.APROBADO))
                .thenReturn(List.of(juez));

        List<JuezSelectDTO> result = adminJuezService.listarJuecesParaSelect();

        assertEquals(1, result.size());
        assertEquals("Ana Perez", result.get(0).nombreCompleto());
    }

    @Test
    void aprobar_activa_usuario_y_competidor() {
        Usuario usuario = Usuario.builder()
                .idUsuario("U1")
                .roles(new HashSet<>(Set.of(RolUsuario.ADMINISTRADOR)))
                .estado(EstadoUsuario.INACTIVO)
                .build();
        Juez juez = Juez.builder()
                .idJuez("J1")
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .build();

        when(juezRepository.findById("J1")).thenReturn(Optional.of(juez));
        when(competidorRepository.findByUsuario_IdUsuario("U1")).thenReturn(Optional.empty());
        when(juezRepository.save(any(Juez.class))).thenAnswer(inv -> inv.getArgument(0));

        Juez result = adminJuezService.aprobar("J1", "ADMIN");

        assertEquals(EstadoValidacion.APROBADO, result.getEstadoValidacion());
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        assertEquals(true, usuario.getRoles().contains(RolUsuario.JUEZ));
        assertEquals(true, usuario.getRoles().contains(RolUsuario.COMPETIDOR));
        verify(competidorRepository, times(1)).save(any(Competidor.class));
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void inactivar_con_pendientes_lanza_error() {
        Usuario usuario = Usuario.builder()
                .idUsuario("U1")
                .roles(new HashSet<>(Set.of(RolUsuario.JUEZ)))
                .build();
        Juez juez = Juez.builder()
                .idJuez("J1")
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();

        when(juezRepository.findById("J1")).thenReturn(Optional.of(juez));
        when(encuentroRepository.countByJuezIdJuezAndEstadoNot("J1", EstadoEncuentro.FINALIZADO))
                .thenReturn(1L);

        assertThrows(RuntimeException.class, () -> adminJuezService.inactivar("J1", "ADMIN"));
        verify(juezRepository, never()).save(any(Juez.class));
    }
}
