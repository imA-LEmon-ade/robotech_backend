package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ResultadoTorneoDTO;
import com.robotech.robotech_backend.dto.TorneoCompetidorDTO;
import com.robotech.robotech_backend.model.entity.CategoriaTorneo;
import com.robotech.robotech_backend.model.entity.EquipoTorneo;
import com.robotech.robotech_backend.model.entity.InscripcionTorneo;
import com.robotech.robotech_backend.model.entity.Torneo;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.HistorialCalificacionRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TorneoCompetidorService {

    private final InscripcionTorneoRepository inscripcionRepo;
    private final EquipoTorneoRepository equipoRepo;
    private final HistorialCalificacionRepository historialRepo;

    private static class TorneoAcc {
        private final Torneo torneo;
        private final Set<String> categorias = new LinkedHashSet<>();
        private final Set<String> modalidades = new LinkedHashSet<>();
        private final Set<String> robots = new LinkedHashSet<>();
        private final Set<String> estadosInscripcion = new LinkedHashSet<>();
        private int inscripciones = 0;

        private TorneoAcc(Torneo torneo) {
            this.torneo = torneo;
        }
    }

    @Transactional(readOnly = true)
    public List<TorneoCompetidorDTO> listarMisTorneos(String idUsuario) {
        Map<String, TorneoAcc> map = new LinkedHashMap<>();

        List<InscripcionTorneo> individuales = inscripcionRepo.findByRobotCompetidorUsuarioIdUsuario(idUsuario);
        for (InscripcionTorneo inscripcion : individuales) {
            CategoriaTorneo categoriaTorneo = inscripcion.getCategoriaTorneo();
            if (categoriaTorneo == null || categoriaTorneo.getTorneo() == null) {
                continue;
            }
            TorneoAcc acc = map.computeIfAbsent(
                    categoriaTorneo.getTorneo().getIdTorneo(),
                    id -> new TorneoAcc(categoriaTorneo.getTorneo())
            );

            if (categoriaTorneo.getCategoria() != null) {
                acc.categorias.add(categoriaTorneo.getCategoria().name());
            }
            if (categoriaTorneo.getModalidad() != null) {
                acc.modalidades.add(categoriaTorneo.getModalidad().name());
            }
            if (inscripcion.getRobot() != null && inscripcion.getRobot().getNombre() != null) {
                acc.robots.add(inscripcion.getRobot().getNombre());
            }
            if (inscripcion.getEstado() != null) {
                acc.estadosInscripcion.add(inscripcion.getEstado().name());
            }
            acc.inscripciones += 1;
        }

        List<EquipoTorneo> equipos = equipoRepo.findByRobotsCompetidorUsuarioIdUsuario(idUsuario);
        for (EquipoTorneo equipo : equipos) {
            CategoriaTorneo categoriaTorneo = equipo.getCategoriaTorneo();
            if (categoriaTorneo == null || categoriaTorneo.getTorneo() == null) {
                continue;
            }
            TorneoAcc acc = map.computeIfAbsent(
                    categoriaTorneo.getTorneo().getIdTorneo(),
                    id -> new TorneoAcc(categoriaTorneo.getTorneo())
            );

            if (categoriaTorneo.getCategoria() != null) {
                acc.categorias.add(categoriaTorneo.getCategoria().name());
            }
            if (categoriaTorneo.getModalidad() != null) {
                acc.modalidades.add(categoriaTorneo.getModalidad().name());
            }
            if (equipo.getRobots() != null) {
                equipo.getRobots().stream()
                        .map(r -> r != null ? r.getNombre() : null)
                        .filter(n -> n != null && !n.isBlank())
                        .forEach(acc.robots::add);
            }
            if (equipo.getEstado() != null) {
                acc.estadosInscripcion.add(equipo.getEstado().name());
            }
            acc.inscripciones += 1;
        }

        List<TorneoCompetidorDTO> result = new ArrayList<>();
        for (TorneoAcc acc : map.values()) {
            Torneo torneo = acc.torneo;
            LocalDate fechaInicio = convertirFecha(torneo.getFechaInicio());
            LocalDate fechaFin = convertirFecha(torneo.getFechaFin());

            String estado = torneo.getEstado() != null ? torneo.getEstado().toString() : "DESCONOCIDO";
            String descripcion = torneo.getDescripcion();

            String ganador = null;
            List<ResultadoTorneoDTO> resultados = new ArrayList<>();
            if ("FINALIZADO".equals(estado)) {
                resultados = historialRepo.obtenerRankingRobots(torneo.getIdTorneo());
                if (resultados != null && !resultados.isEmpty()) {
                    ganador = resultados.get(0).getNombre();
                }
            }

            result.add(TorneoCompetidorDTO.builder()
                    .idTorneo(torneo.getIdTorneo())
                    .nombre(torneo.getNombre())
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .estado(estado)
                    .descripcion(descripcion)
                    .categorias(new ArrayList<>(acc.categorias))
                    .modalidades(new ArrayList<>(acc.modalidades))
                    .robots(new ArrayList<>(acc.robots))
                    .estadosInscripcion(new ArrayList<>(acc.estadosInscripcion))
                    .inscripciones(acc.inscripciones)
                    .ganador(ganador)
                    .resultados(resultados)
                    .build());
        }

        return result;
    }

    private LocalDate convertirFecha(java.util.Date fecha) {
        if (fecha == null) return null;
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}


