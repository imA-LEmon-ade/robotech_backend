package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearTorneoDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Torneo;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoEquipoTorneo;
import com.robotech.robotech_backend.model.enums.EstadoInscripcion;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import com.robotech.robotech_backend.repository.TorneoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TorneoServiceTest {

    @Mock private TorneoRepository torneoRepo;
    @Mock private CategoriaTorneoRepository categoriaRepo;
    @Mock private ClubRepository clubRepo;
    @Mock private EquipoTorneoRepository equipoRepo;
    @Mock private InscripcionTorneoRepository inscripcionRepo;

    @InjectMocks
    private TorneoService service;

    @Test
    void crearTorneo_ok_con_auth() {
        CrearTorneoDTO dto = new CrearTorneoDTO();
        dto.setNombre("Torneo A");
        dto.setDescripcion("Desc");
        dto.setFechaInicio(LocalDateTime.now());
        dto.setFechaFin(LocalDateTime.now().plusDays(1));

        Usuario usuario = Usuario.builder().idUsuario("U1").roles(Set.of(RolUsuario.ADMINISTRADOR)).build();
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuario);
        when(torneoRepo.save(any(Torneo.class))).thenAnswer(inv -> inv.getArgument(0));

        Torneo t = service.crearTorneo(dto, auth);

        assertEquals("U1", t.getCreadoPor());
        assertEquals("BORRADOR", t.getEstado());
    }

    @Test
    void cambiarEstado_invalido_lanza_error() {
        Torneo t = Torneo.builder().idTorneo("T1").estado("BORRADOR").build();
        when(torneoRepo.findById("T1")).thenReturn(Optional.of(t));

        assertThrows(RuntimeException.class, () -> service.cambiarEstado("T1", "DESCONOCIDO"));
    }

    @Test
    void listarDisponiblesParaClub_filtra_por_inscripciones() {
        Club club = Club.builder().idClub("C1").build();
        Torneo t = Torneo.builder().idTorneo("T1").estado("INSCRIPCIONES_ABIERTAS").build();

        when(clubRepo.findByUsuario_IdUsuario("UCLUB")).thenReturn(Optional.of(club));
        when(torneoRepo.findByEstado("INSCRIPCIONES_ABIERTAS")).thenReturn(List.of(t));
        when(equipoRepo.existsByClubIdClubAndCategoriaTorneoTorneoIdTorneoAndEstadoNot("C1", "T1", EstadoEquipoTorneo.ANULADA)).thenReturn(false);
        when(inscripcionRepo.existsByRobotCompetidorClubActualIdClubAndCategoriaTorneoTorneoIdTorneoAndEstadoNot("C1", "T1", EstadoInscripcion.ANULADA)).thenReturn(false);

        List<Torneo> result = service.listarDisponiblesParaClub("UCLUB");
        assertEquals(1, result.size());
    }
}
