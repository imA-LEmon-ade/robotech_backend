package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RankingDTO;
import com.robotech.robotech_backend.model.EquipoTorneo;
import com.robotech.robotech_backend.model.HistorialCalificacion;
import com.robotech.robotech_backend.model.Robot;
import com.robotech.robotech_backend.model.TipoParticipante;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.HistorialCalificacionRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private final HistorialCalificacionRepository historialRepo;
    private final RobotRepository robotRepo;
    private final EquipoTorneoRepository equipoRepo;

    public List<RankingDTO> obtenerRanking(
            TipoParticipante tipo,
            String idCategoriaTorneo
    ) {

        List<HistorialCalificacion> historial =
                historialRepo.findByEncuentroCategoriaTorneoIdCategoriaTorneo(
                        idCategoriaTorneo
                );

        Map<String, List<HistorialCalificacion>> agrupado =
                historial.stream()
                        .filter(h -> h.getTipo() == tipo)
                        .collect(Collectors.groupingBy(
                                HistorialCalificacion::getIdReferencia
                        ));

        List<RankingDTO> ranking = new ArrayList<>();

        for (var entry : agrupado.entrySet()) {

            String idReferencia = entry.getKey();
            List<HistorialCalificacion> lista = entry.getValue();

            long victorias = lista.stream()
                    .filter(h -> h.getPuntaje() == 100)
                    .count();

            long derrotas = lista.stream()
                    .filter(h -> h.getPuntaje() < 100)
                    .count();

            double promedio = lista.stream()
                    .mapToInt(HistorialCalificacion::getPuntaje)
                    .average()
                    .orElse(0);

            int puntosRanking = (int) (victorias * 3);

            String nombre = obtenerNombre(tipo, idReferencia);

            ranking.add(new RankingDTO(
                    idReferencia,
                    nombre,
                    (int) victorias,
                    0,
                    (int) derrotas,
                    promedio,
                    puntosRanking
            ));
        }

        return ranking.stream()
                .sorted(
                        Comparator.comparing(RankingDTO::getPuntosRanking)
                                .reversed()
                                .thenComparing(RankingDTO::getPromedioPuntaje)
                                .reversed()
                )
                .toList();
    }

    private String obtenerNombre(TipoParticipante tipo, String idReferencia) {

        if (tipo == TipoParticipante.ROBOT) {
            return robotRepo.findById(idReferencia)
                    .map(Robot::getNombre)
                    .orElse(idReferencia);
        }

        return equipoRepo.findById(idReferencia)
                .map(EquipoTorneo::getNombre)
                .orElse(idReferencia);
    }


}
