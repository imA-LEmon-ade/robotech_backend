package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CodigoRegistroCompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock private ClubRepository clubRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RobotRepository robotRepository;
    @Mock private CodigoRegistroCompetidorRepository codigoRegistroCompetidorRepository;

    @InjectMocks
    private ClubService clubService;

    @Test
    void crear_usa_usuario_existente() {
        Usuario usuario = Usuario.builder().idUsuario("U1").build();
        Club club = Club.builder().usuario(Usuario.builder().idUsuario("U1").build()).build();

        when(usuarioRepository.findById("U1")).thenReturn(Optional.of(usuario));
        when(clubRepository.save(any(Club.class))).thenAnswer(inv -> inv.getArgument(0));

        Club created = clubService.crear(club);

        assertEquals(usuario, created.getUsuario());
    }

    @Test
    void crear_usuario_no_existe_lanza_error() {
        Club club = Club.builder().usuario(Usuario.builder().idUsuario("U1").build()).build();
        when(usuarioRepository.findById("U1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clubService.crear(club));
    }

    @Test
    void obtenerPorUsuario_devuelve_club() {
        Usuario usuario = Usuario.builder().idUsuario("U1").build();
        Club club = Club.builder().idClub("C1").usuario(usuario).build();

        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuario);
        when(clubRepository.findByUsuario(usuario)).thenReturn(Optional.of(club));

        Club result = clubService.obtenerPorUsuario(auth);

        assertEquals("C1", result.getIdClub());
    }

    @Test
    void obtenerEstadisticasDashboard_mapea_conteos() {
        when(usuarioRepository.contarUsuariosPorClub("C1")).thenReturn(5L);
        when(robotRepository.contarRobotsPorClub("C1")).thenReturn(8L);
        when(codigoRegistroCompetidorRepository.countByClubIdClub("C1")).thenReturn(12L);

        Map<String, Long> stats = clubService.obtenerEstadisticasDashboard("C1");

        assertEquals(5L, stats.get("totalCompetidores"));
        assertEquals(8L, stats.get("totalRobots"));
        assertEquals(12L, stats.get("totalCodigos"));
    }
}
