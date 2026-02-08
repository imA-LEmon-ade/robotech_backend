package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearJuezDTO;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Juez;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.JuezRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.EmailValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubAdminJuezServiceTest {

    @Mock private JuezRepository juezRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private CompetidorRepository competidorRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailValidator emailValidator;
    @Mock private TelefonoValidator telefonoValidator;
    @Mock private DniValidator dniValidator;

    @InjectMocks
    private SubAdminJuezService subAdminJuezService;

    @Test
    void crearJuez_ok_normaliza_correo() {
        CrearJuezDTO dto = new CrearJuezDTO();
        dto.setDni("12345678");
        dto.setNombres("Ana");
        dto.setApellidos("Perez");
        dto.setCorreo(" ANA@EXAMPLE.COM ");
        dto.setTelefono("999111222");
        dto.setLicencia("LIC-01");
        dto.setContrasena("Pass1!");

        when(usuarioRepo.existsByCorreoIgnoreCase("ana@example.com")).thenReturn(false);
        when(usuarioRepo.existsByTelefono("999111222")).thenReturn(false);
        when(usuarioRepo.existsByDni("12345678")).thenReturn(false);
        when(juezRepo.existsByUsuario_IdUsuario("12345678")).thenReturn(false);
        when(passwordEncoder.encode("Pass1!")).thenReturn("hash");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(competidorRepo.save(any(Competidor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(juezRepo.save(any(Juez.class))).thenAnswer(inv -> inv.getArgument(0));

        subAdminJuezService.crearJuez(dto);

        verify(usuarioRepo, times(1)).save(any(Usuario.class));
        verify(competidorRepo, times(1)).save(any(Competidor.class));
        verify(juezRepo, times(1)).save(any(Juez.class));
    }

    @Test
    void crearJuez_correo_duplicado_lanza_error() {
        CrearJuezDTO dto = new CrearJuezDTO();
        dto.setCorreo("ana@example.com");
        dto.setTelefono("999111222");
        dto.setDni("12345678");

        when(usuarioRepo.existsByCorreoIgnoreCase("ana@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> subAdminJuezService.crearJuez(dto));
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }
}
