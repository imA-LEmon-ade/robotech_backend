package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ClubResponseDTO;
import com.robotech.robotech_backend.dto.CrearClubDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoClub;
import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.EmailSuggestionService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubAdminClubServiceTest {

    @Mock private ClubRepository clubRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private CompetidorRepository competidorRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailValidator emailValidator;
    @Mock private TelefonoValidator telefonoValidator;
    @Mock private DniValidator dniValidator;
    @Mock private EmailSuggestionService emailSuggestionService;

    @InjectMocks
    private SubAdminClubService subAdminClubService;

    @Test
    void crearClub_ok() {
        CrearClubDTO dto = new CrearClubDTO();
        dto.setNombre("Robotech");
        dto.setCorreoContacto("CONTACTO@EXAMPLE.COM");
        dto.setTelefonoContacto("999111222");
        dto.setDireccionFiscal("Av 123");
        dto.setDniPropietario("12345678");
        dto.setNombresPropietario("Ana");
        dto.setApellidosPropietario("Perez");
        dto.setCorreoPropietario("OWNER@EXAMPLE.COM");
        dto.setContrasenaPropietario("Pass1!");
        dto.setTelefonoPropietario("999333444");

        when(clubRepo.existsByNombreIgnoreCase(dto.getNombre())).thenReturn(false);
        when(clubRepo.existsByCorreoContacto("contacto@example.com")).thenReturn(false);
        when(clubRepo.existsByTelefonoContacto(dto.getTelefonoContacto())).thenReturn(false);
        when(usuarioRepo.existsByCorreoIgnoreCase("owner@example.com")).thenReturn(false);
        when(usuarioRepo.existsByTelefono(dto.getTelefonoPropietario())).thenReturn(false);
        when(usuarioRepo.existsByDni(dto.getDniPropietario())).thenReturn(false);
        when(passwordEncoder.encode(dto.getContrasenaPropietario())).thenReturn("hash");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clubRepo.save(any(Club.class))).thenAnswer(inv -> {
            Club c = inv.getArgument(0);
            c.setIdClub("C1");
            c.setEstado(EstadoClub.ACTIVO);
            return c;
        });

        ClubResponseDTO resp = subAdminClubService.crearClub(dto);

        assertEquals("C1", resp.getIdClub());
        assertEquals("contacto@example.com", resp.getCorreoContacto());
        assertEquals("owner@example.com", resp.getCorreoPropietario());
        verify(competidorRepo, times(1)).save(any(Competidor.class));
    }

    @Test
    void crearClub_correo_propietario_duplicado_lanza_error() {
        CrearClubDTO dto = new CrearClubDTO();
        dto.setNombre("Robotech");
        dto.setCorreoPropietario("owner@example.com");
        dto.setCorreoContacto("contacto@example.com");
        dto.setTelefonoContacto("999111222");
        dto.setTelefonoPropietario("999333444");
        dto.setDniPropietario("12345678");

        when(usuarioRepo.existsByCorreoIgnoreCase("owner@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> subAdminClubService.crearClub(dto));
    }
}
