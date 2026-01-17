package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.InscripcionResumenDTO;
import com.robotech.robotech_backend.model.EstadoInscripcion;
import com.robotech.robotech_backend.model.InscripcionTorneo;
import com.robotech.robotech_backend.model.EquipoTorneo;
import com.robotech.robotech_backend.model.Robot;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class InscripcionesConsultaService {

    private final InscripcionTorneoRepository inscripcionRepo;
    private final EquipoTorneoRepository equipoRepo;

    // --------------------------------------------------
    // VISTA CLUB
    // --------------------------------------------------
    public List<InscripcionResumenDTO> listarInscripcionesClub(String idClub) {
        return listarInscripcionesClub(idClub, null, null);
    }

    public List<InscripcionResumenDTO> listarInscripcionesClub(String idClub, String busqueda, String estadoFilter) {
        List<InscripcionResumenDTO> individuales = inscripcionRepo
                .findByRobotCompetidorClubActualUsuarioIdUsuario(idClub)
                .stream()
                .map(this::mapIndividualToDTO)
                .toList();

        List<InscripcionResumenDTO> equipos = equipoRepo
                .findByClubUsuarioIdUsuario(idClub)
                .stream()
                .map(this::mapEquipoToDTO)
                .toList();

        return Stream.concat(individuales.stream(), equipos.stream())
                .filter(dto -> aplicarFiltros(dto, busqueda, estadoFilter))
                .toList();
    }

    // --------------------------------------------------
    // VISTA COMPETIDOR (CORREGIDA)
    // --------------------------------------------------

    // Sobrecarga para no romper llamadas antiguas si las hay
    public List<InscripcionResumenDTO> listarInscripcionesCompetidor(String idUsuario) {
        return listarInscripcionesCompetidor(idUsuario, null, null);
    }

    // Método con filtros para el competidor
    public List<InscripcionResumenDTO> listarInscripcionesCompetidor(String idUsuario, String busqueda, String estadoFilter) {

        // 1. Individuales: Usamos el nuevo método del repositorio sin el filtro "ACTIVA" fijo
        List<InscripcionResumenDTO> individuales = inscripcionRepo
                .findByRobotCompetidorUsuarioIdUsuario(idUsuario)
                .stream()
                .map(this::mapIndividualToDTO)
                .toList();

        // 2. Equipos: Traemos los equipos donde participa el competidor
        List<InscripcionResumenDTO> equipos = equipoRepo
                .findByRobotsCompetidorUsuarioIdUsuario(idUsuario)
                .stream()
                .map(this::mapEquipoToDTO)
                .toList();

        // 3. Unimos y aplicamos la misma lógica de filtros (Server-side)
        return Stream.concat(individuales.stream(), equipos.stream())
                .filter(dto -> aplicarFiltros(dto, busqueda, estadoFilter))
                .toList();
    }

    // --------------------------------------------------
    // VISTA ADMIN
    // --------------------------------------------------
    public List<InscripcionResumenDTO> listarTodas() {
        List<InscripcionResumenDTO> individuales = inscripcionRepo.findAll().stream()
                .map(this::mapIndividualToDTO)
                .toList();

        List<InscripcionResumenDTO> equipos = equipoRepo.findAll().stream()
                .map(this::mapEquipoToDTO)
                .toList();

        return Stream.concat(individuales.stream(), equipos.stream()).toList();
    }

    // =============================================================
    // HELPERS PRIVADOS (Lógica corregida)
    // =============================================================

    private boolean aplicarFiltros(InscripcionResumenDTO dto, String busqueda, String estadoFilter) {
        if (estadoFilter != null && !estadoFilter.equalsIgnoreCase("TODOS") && !estadoFilter.isEmpty()) {
            if (!dto.getEstado().equalsIgnoreCase(estadoFilter)) {
                return false;
            }
        }

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            String term = busqueda.toLowerCase().trim();
            String torneo = dto.getTorneo() != null ? dto.getTorneo().toLowerCase() : "";
            boolean matchTorneo = torneo.contains(term);
            boolean matchRobot = dto.getRobots() != null && dto.getRobots().stream()
                    .anyMatch(r -> r != null && r.toLowerCase().contains(term));
            return matchTorneo || matchRobot;
        }

        return true;
    }

    private InscripcionResumenDTO mapIndividualToDTO(InscripcionTorneo i) {
        java.time.LocalDate fecha = null;
        if (i.getFechaInscripcion() != null) {
            fecha = i.getFechaInscripcion().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        return InscripcionResumenDTO.builder()
                .idInscripcion(i.getIdInscripcion())
                .torneo(i.getCategoriaTorneo().getTorneo().getNombre())
                .categoria(i.getCategoriaTorneo().getCategoria().name())
                .modalidad("INDIVIDUAL")
                .robots(Collections.singletonList(i.getRobot().getNombre()))
                .estado(i.getEstado().name())
                .fechaRegistro(fecha)
                .torneoFecha(i.getCategoriaTorneo().getTorneo().getFechaInicio().toString())
                .build();
    }

    private InscripcionResumenDTO mapEquipoToDTO(EquipoTorneo e) {
        return InscripcionResumenDTO.builder()
                .idInscripcion(e.getIdEquipo())
                .torneo(e.getCategoriaTorneo().getTorneo().getNombre())
                .categoria(e.getCategoriaTorneo().getCategoria().name())
                .modalidad("EQUIPO")
                .robots(e.getRobots().stream().map(Robot::getNombre).toList())
                .estado(e.getEstado().name())
                .fechaRegistro(java.time.LocalDate.now())
                .torneoFecha(e.getCategoriaTorneo().getTorneo().getFechaInicio().toString())
                .build();
    }
}