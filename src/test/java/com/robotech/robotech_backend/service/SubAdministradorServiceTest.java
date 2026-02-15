package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearSubAdminDTO;
import com.robotech.robotech_backend.dto.EditarSubAdminDTO;
import com.robotech.robotech_backend.dto.SubAdminResponseDTO;
import com.robotech.robotech_backend.model.entity.SubAdministrador;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoSubAdmin;
import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.repository.SubAdministradorRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubAdministradorServiceTest {

    @Mock private SubAdministradorRepository subAdminRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DniValidator dniValidator;
    @Mock private TelefonoValidator telefonoValidator;

    @InjectMocks
    private SubAdministradorService subAdminService;

    @Test
    void crear_ok_mapea_respuesta() {
        CrearSubAdminDTO dto = new CrearSubAdminDTO();
        dto.setDni("12345678");
        dto.setNombres("Ana");
        dto.setApellidos("Perez");
        dto.setCorreo("ana@robotech.com");
        dto.setTelefono("999111222");
        dto.setContrasena("Pass1!");

        when(usuarioRepo.existsByCorreoIgnoreCase("ana@robotech.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass1!")).thenReturn("hash");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario("U1");
            u.setEstado(EstadoUsuario.ACTIVO);
            u.setRoles(Set.of(RolUsuario.SUBADMINISTRADOR));
            return u;
        });
        when(subAdminRepo.save(any(SubAdministrador.class))).thenAnswer(inv -> {
            SubAdministrador s = inv.getArgument(0);
            s.setIdUsuario("U1");
            return s;
        });

        SubAdminResponseDTO resp = subAdminService.crear(dto);

        assertEquals("U1", resp.getIdSubadmin());
        assertEquals("Ana", resp.getNombres());
        assertEquals(EstadoSubAdmin.ACTIVO, resp.getEstado());
    }

    @Test
    void listarTodos_usa_buscar() {
        Usuario u = Usuario.builder().nombres("Ana").apellidos("Perez").correo("a@a.com").dni("123").build();
        SubAdministrador s = SubAdministrador.builder().idUsuario("U1").usuario(u).estado(EstadoSubAdmin.ACTIVO).build();
        Pageable pageable = PageRequest.of(0, 20);
        Page<SubAdministrador> page = new PageImpl<>(List.of(s), pageable, 1);

        when(subAdminRepo.buscar("ana", pageable)).thenReturn(page);

        Page<SubAdminResponseDTO> result = subAdminService.listarTodos(pageable, "ana", null, null, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("U1", result.getContent().get(0).getIdSubadmin());
    }

    @Test
    void cambiarEstado_actualiza_y_guarda() {
        Usuario u = Usuario.builder().idUsuario("U1").build();
        SubAdministrador sub = SubAdministrador.builder().idUsuario("U1").usuario(u).estado(EstadoSubAdmin.ACTIVO).build();
        when(subAdminRepo.findById("U1")).thenReturn(Optional.of(sub));
        when(subAdminRepo.save(any(SubAdministrador.class))).thenAnswer(inv -> inv.getArgument(0));

        SubAdminResponseDTO resp = subAdminService.cambiarEstado("U1", EstadoSubAdmin.INACTIVO);

        assertEquals(EstadoSubAdmin.INACTIVO, resp.getEstado());
    }

    @Test
    void editar_actualiza_usuario() {
        Usuario u = Usuario.builder().nombres("Ana").apellidos("Perez").telefono("999").build();
        SubAdministrador sub = SubAdministrador.builder().idUsuario("U1").usuario(u).estado(EstadoSubAdmin.ACTIVO).build();

        EditarSubAdminDTO dto = new EditarSubAdminDTO();
        dto.setNombres("Ana Maria");
        dto.setApellidos("Perez");
        dto.setTelefono("999111222");

        when(subAdminRepo.findById("U1")).thenReturn(Optional.of(sub));

        SubAdminResponseDTO resp = subAdminService.editar("U1", dto);

        assertEquals("Ana Maria", resp.getNombres());
        verify(usuarioRepo, times(1)).save(u);
    }
}
