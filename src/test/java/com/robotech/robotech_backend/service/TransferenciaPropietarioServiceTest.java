package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.TransferenciaPropietarioCrearDTO;
import com.robotech.robotech_backend.dto.TransferenciaPropietarioDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.EstadoTransferenciaPropietario;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.TransferenciaPropietarioRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferenciaPropietarioServiceTest {

    @Mock private TransferenciaPropietarioRepository transferenciaRepo;
    @Mock private ClubRepository clubRepo;
    @Mock private CompetidorRepository competidorRepo;
    @Mock private UsuarioRepository usuarioRepo;

    @InjectMocks
    private TransferenciaPropietarioService service;

    @Test
    void solicitar_ok() {
        TransferenciaPropietarioCrearDTO dto = new TransferenciaPropietarioCrearDTO();
        dto.setIdCompetidor("COMP1");

        Usuario owner = Usuario.builder().idUsuario("U1").build();
        Club club = Club.builder().idClub("C1").usuario(owner).build();
        Usuario nuevo = Usuario.builder().idUsuario("U2").build();
        Competidor comp = Competidor.builder().idCompetidor("COMP1").usuario(nuevo).clubActual(club).estadoValidacion(EstadoValidacion.APROBADO).build();

        when(clubRepo.findByUsuario_IdUsuario("UCLUB")).thenReturn(Optional.of(club));
        when(transferenciaRepo.existsByClub_IdClubAndEstado("C1", EstadoTransferenciaPropietario.PENDIENTE)).thenReturn(false);
        when(competidorRepo.findById("COMP1")).thenReturn(Optional.of(comp));
        when(transferenciaRepo.save(any(TransferenciaPropietario.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferenciaPropietarioDTO resp = service.solicitar("UCLUB", dto);

        assertEquals(EstadoTransferenciaPropietario.PENDIENTE, resp.getEstado());
    }

    @Test
    void aprobar_ok_cambia_roles_y_owner() {
        Usuario owner = Usuario.builder().idUsuario("U1").roles(new java.util.HashSet<>(Set.of(RolUsuario.CLUB))).build();
        Usuario nuevo = Usuario.builder().idUsuario("U2").roles(new java.util.HashSet<>(Set.of(RolUsuario.COMPETIDOR))).build();
        Club club = Club.builder().idClub("C1").usuario(owner).build();
        Competidor comp = Competidor.builder().idCompetidor("COMP1").usuario(nuevo).clubActual(club).estadoValidacion(EstadoValidacion.APROBADO).build();

        TransferenciaPropietario t = TransferenciaPropietario.builder()
                .idTransferencia("T1")
                .club(club)
                .propietarioActual(owner)
                .competidorNuevo(comp)
                .estado(EstadoTransferenciaPropietario.PENDIENTE)
                .build();

        when(transferenciaRepo.findById("T1")).thenReturn(Optional.of(t));
        when(clubRepo.findByUsuario(nuevo)).thenReturn(Optional.empty());
        when(transferenciaRepo.save(any(TransferenciaPropietario.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferenciaPropietarioDTO resp = service.aprobar("T1");

        assertEquals(EstadoTransferenciaPropietario.APROBADA, resp.getEstado());
        verify(usuarioRepo, times(2)).save(any(Usuario.class));
        verify(clubRepo, times(1)).save(club);
    }
}
