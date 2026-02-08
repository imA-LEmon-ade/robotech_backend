package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ClubResponseDTO;
import com.robotech.robotech_backend.dto.CrearClubDTO;
import com.robotech.robotech_backend.dto.EditarClubDTO;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminClubServiceTest {

    @Mock private ClubRepository clubRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private CompetidorRepository competidorRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailValidator emailValidator;
    @Mock private TelefonoValidator telefonoValidator;
    @Mock private DniValidator dniValidator;
    @Mock private EmailSuggestionService emailSuggestionService;

    @InjectMocks
    private AdminClubService adminClubService;

    @Test
    void crearClub_ok_saves_entities_and_maps() {
        CrearClubDTO dto = new CrearClubDTO();
        dto.setNombre("Robotech");
        dto.setCorreoContacto("CONTACTO@EXAMPLE.COM");
        dto.setTelefonoContacto("999111222");
        dto.setDireccionFiscal("Av 123");
        dto.setDniPropietario("12345678");
        dto.setNombresPropietario("Ana");
        dto.setApellidosPropietario("Perez");
        dto.setCorreoPropietario("OWNER@EXAMPLE.COM ");
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
            return c;
        });

        ClubResponseDTO resp = adminClubService.crearClub(dto);

        verify(emailValidator, times(1)).validar("owner@example.com");
        verify(emailValidator, times(1)).validar("contacto@example.com");
        verify(telefonoValidator, times(1)).validar("999333444");
        verify(telefonoValidator, times(1)).validar("999111222");

        ArgumentCaptor<Club> clubCaptor = ArgumentCaptor.forClass(Club.class);
        verify(clubRepo, times(1)).save(clubCaptor.capture());

        Club savedClub = clubCaptor.getValue();
        assertEquals("Robotech", savedClub.getNombre());
        assertEquals("contacto@example.com", savedClub.getCorreoContacto());
        assertEquals("999111222", savedClub.getTelefonoContacto());
        assertNotNull(savedClub.getUsuario());
        assertEquals("owner@example.com", savedClub.getUsuario().getCorreo());

        verify(competidorRepo, times(1)).save(any(Competidor.class));

        assertEquals("C1", resp.getIdClub());
        assertEquals("Robotech", resp.getNombre());
        assertEquals("contacto@example.com", resp.getCorreoContacto());
        assertEquals("owner@example.com", resp.getCorreoPropietario());
        assertEquals("12345678", resp.getDniPropietario());
    }

    @Test
    void listar_trims_busqueda_and_maps() {
        Usuario owner = Usuario.builder()
                .idUsuario("U1")
                .dni("12345678")
                .nombres("Ana")
                .apellidos("Perez")
                .correo("ana@robotech.com")
                .telefono("999111222")
                .build();

        Club club = Club.builder()
                .idClub("C1")
                .codigoClub("CLUB01")
                .nombre("Robotech")
                .correoContacto("club@robotech.com")
                .telefonoContacto("999000111")
                .direccionFiscal("Av 123")
                .estado(EstadoClub.ACTIVO)
                .usuario(owner)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        Page<Club> page = new PageImpl<>(List.of(club), pageable, 1);
        when(clubRepo.buscarPorNombre(eq("rob"), eq(pageable))).thenReturn(page);

        Page<ClubResponseDTO> result = adminClubService.listar("  rob  ", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Robotech", result.getContent().get(0).getNombre());
    }

    @Test
    void editar_cambia_estado_inactivo_actualiza_usuarios() {
        Usuario owner = Usuario.builder()
                .idUsuario("U1")
                .dni("12345678")
                .nombres("Ana")
                .apellidos("Perez")
                .correo("ana@robotech.com")
                .telefono("999111222")
                .estado(EstadoUsuario.ACTIVO)
                .roles(Set.of(RolUsuario.CLUB))
                .build();

        Club club = Club.builder()
                .idClub("C1")
                .nombre("Robotech")
                .correoContacto("club@robotech.com")
                .telefonoContacto("999000111")
                .estado(EstadoClub.ACTIVO)
                .usuario(owner)
                .build();

        Usuario compUser = Usuario.builder()
                .idUsuario("U2")
                .estado(EstadoUsuario.ACTIVO)
                .build();
        Competidor comp = Competidor.builder()
                .usuario(compUser)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();

        EditarClubDTO dto = new EditarClubDTO();
        dto.setNombre("Robotech");
        dto.setCorreoContacto("club@robotech.com");
        dto.setTelefonoContacto("999000111");
        dto.setDireccionFiscal("Av 123");
        dto.setEstado(EstadoClub.INACTIVO);

        when(clubRepo.findById("C1")).thenReturn(java.util.Optional.of(club));
        when(competidorRepo.findByClubActual_IdClub("C1")).thenReturn(List.of(comp));

        doNothing().when(emailValidator).validar("club@robotech.com");
        doNothing().when(telefonoValidator).validar("999000111");

        ClubResponseDTO resp = adminClubService.editar("C1", dto);

        assertEquals(EstadoClub.INACTIVO, resp.getEstado());
        assertEquals(EstadoUsuario.INACTIVO, owner.getEstado());
        assertEquals(EstadoUsuario.INACTIVO, compUser.getEstado());

        verify(usuarioRepo, times(2)).save(any(Usuario.class));
        verify(clubRepo, times(1)).save(any(Club.class));
    }
}
