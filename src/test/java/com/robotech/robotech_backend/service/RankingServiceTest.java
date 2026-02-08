package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RankingDTO;
import com.robotech.robotech_backend.model.entity.EncuentroParticipante;
import com.robotech.robotech_backend.model.entity.HistorialCalificacion;
import com.robotech.robotech_backend.model.entity.Robot;
import com.robotech.robotech_backend.model.enums.TipoParticipante;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.EncuentroParticipanteRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.HistorialCalificacionRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock private HistorialCalificacionRepository historialRepo;
    @Mock private RobotRepository robotRepo;
    @Mock private EquipoTorneoRepository equipoRepo;
    @Mock private EncuentroParticipanteRepository participanteRepo;
    @Mock private CompetidorRepository competidorRepo;

    @InjectMocks
    private RankingService rankingService;

    @Test
    void obtenerRankingGlobalRobots_suma_puntos_y_victorias() {
        EncuentroParticipante p1 = EncuentroParticipante.builder()
                .tipo(TipoParticipante.ROBOT)
                .idReferencia("R1")
                .ganador(true)
                .build();
        EncuentroParticipante p2 = EncuentroParticipante.builder()
                .tipo(TipoParticipante.ROBOT)
                .idReferencia("R1")
                .ganador(false)
                .build();

        HistorialCalificacion h1 = HistorialCalificacion.builder()
                .tipo(TipoParticipante.ROBOT)
                .idReferencia("R1")
                .puntaje(10)
                .build();

        when(participanteRepo.findAll()).thenReturn(List.of(p1, p2));
        when(historialRepo.findAll()).thenReturn(List.of(h1));
        when(robotRepo.findById("R1")).thenReturn(Optional.of(Robot.builder().nombre("Titan").build()));

        List<RankingDTO> result = rankingService.obtenerRankingGlobalRobots();

        assertEquals(1, result.size());
        RankingDTO dto = result.get(0);
        assertEquals(10, dto.getPuntosRanking());
        assertEquals(1, dto.getVictorias());
        assertEquals(1, dto.getDerrotas());
        assertEquals(5.0, dto.getPromedioPuntaje());
    }
}
