package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.TransferenciaCrearDTO;
import com.robotech.robotech_backend.dto.TransferenciaDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.TransferenciaCompetidor;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoTransferencia;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.TransferenciaCompetidorRepository;
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
class TransferenciaCompetidorServiceTest {

    @Mock private TransferenciaCompetidorRepository transferenciaRepo;
    @Mock private CompetidorRepository competidorRepo;
    @Mock private ClubRepository clubRepo;

    @InjectMocks
    private TransferenciaCompetidorService service;

    @Test
    void publicar_ok() {
        TransferenciaCrearDTO dto = new TransferenciaCrearDTO();
        dto.setIdCompetidor("COMP1");
        dto.setPrecio(100);

        Club club = Club.builder().idClub("C1").build();
        Usuario usuario = Usuario.builder().idUsuario("U1").build();
        Competidor comp = Competidor.builder().idCompetidor("COMP1").usuario(usuario).clubActual(club).build();

        when(clubRepo.findByUsuario_IdUsuario("UCLUB")).thenReturn(Optional.of(club));
        when(competidorRepo.findById("COMP1")).thenReturn(Optional.of(comp));
        when(transferenciaRepo.existsByCompetidor_IdCompetidorAndEstadoIn("COMP1", Set.of(EstadoTransferencia.EN_VENTA, EstadoTransferencia.PENDIENTE))).thenReturn(false);
        when(transferenciaRepo.save(any(TransferenciaCompetidor.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferenciaDTO resp = service.publicar("UCLUB", dto);

        assertEquals(EstadoTransferencia.EN_VENTA, resp.getEstado());
    }

    @Test
    void solicitar_ok_cambia_estado() {
        Club origen = Club.builder().idClub("C1").build();
        Club destino = Club.builder().idClub("C2").build();
        TransferenciaCompetidor t = TransferenciaCompetidor.builder()
                .idTransferencia("T1")
                .clubOrigen(origen)
                .estado(EstadoTransferencia.EN_VENTA)
                .build();

        when(clubRepo.findByUsuario_IdUsuario("UDEST")).thenReturn(Optional.of(destino));
        when(transferenciaRepo.findById("T1")).thenReturn(Optional.of(t));
        when(transferenciaRepo.save(any(TransferenciaCompetidor.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferenciaDTO resp = service.solicitar("UDEST", "T1");

        assertEquals(EstadoTransferencia.PENDIENTE, resp.getEstado());
    }
}
