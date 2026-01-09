package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.InscripcionResumenDTO;
import com.robotech.robotech_backend.model.EstadoInscripcion;
import com.robotech.robotech_backend.model.Robot;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public List<InscripcionResumenDTO> listarInscripcionesClub(String idUsuarioClub) {

        List<InscripcionResumenDTO> individuales =
                inscripcionRepo
                        .findByRobotCompetidorClubActualUsuarioIdUsuarioAndEstado(
                                idUsuarioClub,
                                EstadoInscripcion.ACTIVA
                        )
                        .stream()
                        .map(i -> new InscripcionResumenDTO(
                                i.getIdInscripcion(),
                                i.getCategoriaTorneo().getTorneo().getNombre(),
                                i.getCategoriaTorneo().getCategoria().name(),
                                "INDIVIDUAL",
                                List.of(i.getRobot().getNombre()),
                                i.getEstado().name()
                        ))
                        .toList();

        List<InscripcionResumenDTO> equipos =
                equipoRepo.findByClubUsuarioIdUsuario(idUsuarioClub)
                        .stream()
                        .map(e -> new InscripcionResumenDTO(
                                e.getIdEquipo(),
                                e.getCategoriaTorneo().getTorneo().getNombre(),
                                e.getCategoriaTorneo().getCategoria().name(),
                                "EQUIPO",
                                e.getRobots().stream().map(Robot::getNombre).toList(),
                                e.getEstado()
                        ))
                        .toList();

        return Stream.concat(individuales.stream(), equipos.stream()).toList();
    }

    // --------------------------------------------------
    // VISTA COMPETIDOR
    // --------------------------------------------------
    public List<InscripcionResumenDTO> listarInscripcionesCompetidor(String idUsuario) {

        List<InscripcionResumenDTO> individuales =
                inscripcionRepo
                        .findByRobotCompetidorUsuarioIdUsuarioAndEstado(
                                idUsuario,
                                EstadoInscripcion.ACTIVA
                        )
                        .stream()
                        .map(i -> new InscripcionResumenDTO(
                                i.getIdInscripcion(),
                                i.getCategoriaTorneo().getTorneo().getNombre(),
                                i.getCategoriaTorneo().getCategoria().name(),
                                "INDIVIDUAL",
                                List.of(i.getRobot().getNombre()),
                                i.getEstado().name()
                        ))
                        .toList();

        List<InscripcionResumenDTO> equipos =
                equipoRepo.findByRobotsCompetidorUsuarioIdUsuario(idUsuario)
                        .stream()
                        .map(e -> new InscripcionResumenDTO(
                                e.getIdEquipo(),
                                e.getCategoriaTorneo().getTorneo().getNombre(),
                                e.getCategoriaTorneo().getCategoria().name(),
                                "EQUIPO",
                                e.getRobots().stream().map(Robot::getNombre).toList(),
                                e.getEstado()
                        ))
                        .toList();

        return Stream.concat(individuales.stream(), equipos.stream()).toList();
    }
}
