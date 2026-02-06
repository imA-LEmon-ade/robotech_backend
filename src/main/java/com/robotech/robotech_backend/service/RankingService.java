package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RankingDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private final HistorialCalificacionRepository historialRepo;
    private final RobotRepository robotRepo;
    private final EquipoTorneoRepository equipoRepo;
    private final EncuentroParticipanteRepository participanteRepo;
    private final CompetidorRepository competidorRepo;

    public List<RankingDTO> obtenerRankingGlobalRobots() {
        return procesarRanking(TipoParticipante.ROBOT);
    }

    public List<RankingDTO> obtenerRankingGlobalCompetidores() {
        // ✅ Ahora procesamos basándonos en los robots que pertenecen a cada competidor
        return procesarRankingJerarquico(TipoParticipante.ROBOT, "COMPETIDOR");
    }

    public List<RankingDTO> obtenerRankingGlobalClubes() {
        return procesarRankingJerarquico(TipoParticipante.ROBOT, "CLUB");
    }

    private List<RankingDTO> procesarRanking(TipoParticipante tipo) {
        Map<String, RankingDTO> mapa = new HashMap<>();

        // Procesar Participaciones
        participanteRepo.findAll().stream()
                .filter(p -> p.getTipo() == tipo)
                .forEach(p -> {
                    String id = p.getIdReferencia();
                    RankingDTO dto = mapa.getOrDefault(id, new RankingDTO(id, obtenerNombre(tipo, id), 0, 0, 0, 0.0, 0));
                    if (Boolean.TRUE.equals(p.getGanador())) dto.setVictorias(dto.getVictorias() + 1);
                    else if (Boolean.FALSE.equals(p.getGanador())) dto.setDerrotas(dto.getDerrotas() + 1);
                    mapa.put(id, dto);
                });

        // Procesar Puntos
        historialRepo.findAll().stream()
                .filter(h -> h.getTipo() == tipo)
                .forEach(h -> {
                    String id = h.getIdReferencia();
                    RankingDTO dto = mapa.getOrDefault(id, new RankingDTO(id, obtenerNombre(tipo, id), 0, 0, 0, 0.0, 0));
                    dto.setPuntosRanking(dto.getPuntosRanking() + h.getPuntaje());
                    mapa.put(id, dto);
                });

        return finalizarYOrdenar(mapa);
    }

    private List<RankingDTO> procesarRankingJerarquico(TipoParticipante tipoOrigen, String nivelDestino) {
        Map<String, RankingDTO> mapa = new HashMap<>();

        // 1. Mapear resultados de robots hacia sus dueños (Competidores o Clubes)
        participanteRepo.findAll().stream()
                .filter(p -> p.getTipo() == tipoOrigen)
                .forEach(p -> {
                    Robot robot = robotRepo.findById(p.getIdReferencia()).orElse(null);
                    if (robot != null) {
                        String idDestino = nivelDestino.equals("CLUB") ?
                                (robot.getCompetidor().getClubActual() != null ? robot.getCompetidor().getClubActual().getIdClub() : null) :
                                (robot.getCompetidor() != null ? robot.getCompetidor().getIdCompetidor() : null);

                        if (idDestino != null) {
                            String nombreDestino = nivelDestino.equals("CLUB") ? robot.getCompetidor().getClubActual().getNombre() :
                                    (robot.getCompetidor().getUsuario().getNombres() + " " + robot.getCompetidor().getUsuario().getApellidos());

                            RankingDTO dto = mapa.getOrDefault(idDestino, new RankingDTO(idDestino, nombreDestino, 0, 0, 0, 0.0, 0));
                            if (Boolean.TRUE.equals(p.getGanador())) dto.setVictorias(dto.getVictorias() + 1);
                            else if (Boolean.FALSE.equals(p.getGanador())) dto.setDerrotas(dto.getDerrotas() + 1);
                            mapa.put(idDestino, dto);
                        }
                    }
                });

        // 2. Mapear puntos de robots hacia dueños
        historialRepo.findAll().stream()
                .filter(h -> h.getTipo() == tipoOrigen)
                .forEach(h -> {
                    Robot robot = robotRepo.findById(h.getIdReferencia()).orElse(null);
                    if (robot != null) {
                        String idDestino = nivelDestino.equals("CLUB") ?
                                (robot.getCompetidor().getClubActual() != null ? robot.getCompetidor().getClubActual().getIdClub() : null) :
                                (robot.getCompetidor() != null ? robot.getCompetidor().getIdCompetidor() : null);

                        if (idDestino != null) {
                            RankingDTO dto = mapa.getOrDefault(idDestino, mapa.get(idDestino)); // Evita sobreescribir si no existía en victorias
                            if (dto == null) {
                                String nombreDestino = nivelDestino.equals("CLUB") ? robot.getCompetidor().getClubActual().getNombre() :
                                        (robot.getCompetidor().getUsuario().getNombres() + " " + robot.getCompetidor().getUsuario().getApellidos());
                                dto = new RankingDTO(idDestino, nombreDestino, 0, 0, 0, 0.0, 0);
                            }
                            dto.setPuntosRanking(dto.getPuntosRanking() + h.getPuntaje());
                            mapa.put(idDestino, dto);
                        }
                    }
                });

        return finalizarYOrdenar(mapa);
    }

    private List<RankingDTO> finalizarYOrdenar(Map<String, RankingDTO> mapa) {
        List<RankingDTO> lista = new ArrayList<>(mapa.values());
        lista.forEach(r -> {
            int total = r.getVictorias() + r.getDerrotas();
            r.setPromedioPuntaje(total > 0 ? (double) r.getPuntosRanking() / total : 0.0);
        });
        lista.sort((a, b) -> b.getPuntosRanking().compareTo(a.getPuntosRanking()));
        return lista;
    }

    private String obtenerNombre(TipoParticipante tipo, String id) {
        return robotRepo.findById(id).map(Robot::getNombre).orElse("ID: " + id);
    }

    // El método de categoría lo mantenemos igual o lo adaptamos luego
    public List<RankingDTO> obtenerRankingPorCategoria(TipoParticipante tipo, String id) {
        return procesarRanking(tipo);
    }
}

