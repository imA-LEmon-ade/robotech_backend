package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.SolicitudIngresoCrearDTO;
import com.robotech.robotech_backend.dto.SolicitudIngresoDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudIngresoClubServiceTest {

    @Mock private SolicitudIngresoClubRepository solicitudRepo;
    @Mock private CompetidorRepository competidorRepo;
    @Mock private ClubRepository clubRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private JuezRepository juezRepo;
    @Mock private EncuentroRepository encuentroRepo;

    @InjectMocks
    private SolicitudIngresoClubService service;

    @Test
    void solicitar_ok() {
        SolicitudIngresoCrearDTO dto = new SolicitudIngresoCrearDTO();
        dto.setCodigoClub("CLUB01");

        Club club = Club.builder().idClub("C1").estado(EstadoClub.ACTIVO).build();
        Usuario u = Usuario.builder().idUsuario("U1").build();
        Competidor comp = Competidor.builder().idCompetidor("U1").usuario(u).estadoValidacion(EstadoValidacion.APROBADO).clubActual(null).build();

        when(competidorRepo.findByUsuario_IdUsuario("U1")).thenReturn(Optional.of(comp));
        when(solicitudRepo.existsByCompetidor_IdCompetidorAndEstado("U1", EstadoSolicitudIngresoClub.PENDIENTE)).thenReturn(false);
        when(clubRepo.findByCodigoClub("CLUB01")).thenReturn(Optional.of(club));
        when(solicitudRepo.save(any(SolicitudIngresoClub.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudIngresoDTO resp = service.solicitar("U1", dto);

        assertEquals("C1", resp.getIdClub());
    }

    @Test
    void aprobar_solicitud_actualiza_competidor() {
        Club club = Club.builder().idClub("C1").estado(EstadoClub.ACTIVO).build();
        Usuario usuario = Usuario.builder().idUsuario("U1").roles(Set.of(RolUsuario.COMPETIDOR)).build();
        Competidor comp = Competidor.builder().idCompetidor("U1").usuario(usuario).clubActual(null).estadoValidacion(EstadoValidacion.PENDIENTE).build();
        SolicitudIngresoClub s = SolicitudIngresoClub.builder().idSolicitud("S1").club(club).competidor(comp).estado(EstadoSolicitudIngresoClub.PENDIENTE).build();

        when(clubRepo.findByUsuario_IdUsuario("UCLUB")).thenReturn(Optional.of(club));
        when(solicitudRepo.findById("S1")).thenReturn(Optional.of(s));
        when(solicitudRepo.save(any(SolicitudIngresoClub.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitudIngresoDTO resp = service.aprobar("UCLUB", "S1");

        assertEquals(EstadoSolicitudIngresoClub.APROBADA, resp.getEstado());
        verify(competidorRepo, times(1)).save(comp);
    }
}
