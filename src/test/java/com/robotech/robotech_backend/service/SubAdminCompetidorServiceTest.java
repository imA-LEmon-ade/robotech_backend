package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CompetidorResponseDTO;
import com.robotech.robotech_backend.dto.RegistroCompetidorDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubAdminCompetidorServiceTest {

    @Mock private UsuarioRepository usuarioRepo;
    @Mock private CompetidorRepository competidorRepo;
    @Mock private ClubRepository clubRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DniValidator dniValidator;
    @Mock private TelefonoValidator telefonoValidator;

    @InjectMocks
    private SubAdminCompetidorService subAdminCompetidorService;

    @Test
    void listarTodos_mapea_dto() {
        Usuario u = Usuario.builder()
                .idUsuario("U1")
                .nombres("Ana")
                .apellidos("Perez")
                .dni("12345678")
                .correo("ana@robotech.com")
                .telefono("999111222")
                .build();
        Club club = Club.builder().nombre("Club A").build();
        Competidor comp = Competidor.builder()
                .usuario(u)
                .clubActual(club)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();

        when(competidorRepo.findAll()).thenReturn(List.of(comp));

        List<CompetidorResponseDTO> result = subAdminCompetidorService.listarTodos();

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getNombres());
        assertEquals("Club A", result.get(0).getClubNombre());
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
        dto.setCodigoClub("C1");

        Club club = Club.builder().idClub("C1").build();

        when(usuarioRepo.existsByCorreoIgnoreCase("ana@robotech.com")).thenReturn(false);
        when(clubRepo.findById("C1")).thenReturn(Optional.of(club));
        when(passwordEncoder.encode("Pass1!")).thenReturn("hash");

        subAdminCompetidorService.registrarCompetidor(dto);

        verify(usuarioRepo, times(1)).save(any(Usuario.class));
        verify(competidorRepo, times(1)).save(any(Competidor.class));
    }

    @Test
    void registrarCompetidor_club_no_existe_lanza_error() {
        RegistroCompetidorDTO dto = new RegistroCompetidorDTO();
        dto.setCodigoClub("C1");
        dto.setCorreo("ana@robotech.com");
        dto.setDni("12345678");
        dto.setTelefono("999111222");

        when(usuarioRepo.existsByCorreoIgnoreCase("ana@robotech.com")).thenReturn(false);
        when(clubRepo.findById("C1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> subAdminCompetidorService.registrarCompetidor(dto));
    }
}
