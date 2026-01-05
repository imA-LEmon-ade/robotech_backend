package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.InscripcionIndividualDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscripcionTorneoService {

    private final CategoriaTorneoRepository categoriaRepo;
    private final RobotRepository robotRepo;
    private final InscripcionTorneoRepository inscripcionRepo;
    private final CompetidorRepository competidorRepo;
    private final ClubRepository clubRepo;

    // ----------------------------------------------------------------------
    // INSCRIBIR ROBOT (MODALIDAD INDIVIDUAL)
    // ----------------------------------------------------------------------
    public InscripcionTorneo inscribirIndividualComoClub(
            String idUsuarioClub,
            InscripcionIndividualDTO dto
    ) {

        // 1) Club
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        // 2) Categoria torneo
        CategoriaTorneo categoria = categoriaRepo.findById(dto.getIdCategoriaTorneo())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 3) Validar modalidad
        if (categoria.getModalidad() != ModalidadCategoria.INDIVIDUAL) {
            throw new RuntimeException("Esta categoría no es individual");
        }

        Torneo torneo = categoria.getTorneo();

        // 4) Validar fechas
        Date hoy = new Date();
        if (hoy.before(torneo.getFechaAperturaInscripcion()) ||
                hoy.after(torneo.getFechaCierreInscripcion())) {
            throw new RuntimeException("Las inscripciones están cerradas");
        }

        // 5) Robot
        Robot robot = robotRepo.findById(dto.getIdRobot())
                .orElseThrow(() -> new RuntimeException("Robot no encontrado"));

        // 6) Validar pertenencia al club
        if (!robot.getCompetidor().getClub().getIdClub().equals(club.getIdClub())) {
            throw new RuntimeException("El robot no pertenece a este club");
        }

        // ✅ 6.1) Validar que el robot sea de la misma categoría
        if (robot.getCategoria() != categoria.getCategoria()) {
            throw new RuntimeException(
                    "El robot es de categoría " + robot.getCategoria()
                            + " y la categoría del torneo es " + categoria.getCategoria()
            );
        }

        // 7) Validar duplicado (robot ya inscrito en el torneo)
        boolean yaInscrito = inscripcionRepo
                .existsByRobotIdRobotAndCategoriaTorneoTorneoIdTorneo(
                        robot.getIdRobot(),
                        torneo.getIdTorneo()
                );

        if (yaInscrito) {
            throw new RuntimeException("El robot ya está inscrito en este torneo");
        }

        // 8) Validar cupos
        long inscritos = inscripcionRepo.countByCategoriaTorneoIdCategoriaTorneo(
                categoria.getIdCategoriaTorneo()
        );

        if (inscritos >= categoria.getMaxParticipantes()) {
            throw new RuntimeException("No hay cupos disponibles en esta categoría");
        }

        // 9) Crear inscripción
        InscripcionTorneo inscripcion = InscripcionTorneo.builder()
                .categoriaTorneo(categoria)
                .robot(robot)
                .estado("PENDIENTE")
                .fechaInscripcion(new Date())
                .build();

        return inscripcionRepo.save(inscripcion);
    }



    // ----------------------------------------------------------------------
    // APROBAR INSCRIPCIÓN
    // ----------------------------------------------------------------------
    public InscripcionTorneo aprobar(String idInscripcion) {
        InscripcionTorneo i = inscripcionRepo.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        i.setEstado("APROBADO");
        return inscripcionRepo.save(i);
    }

    // ----------------------------------------------------------------------
    // RECHAZAR INSCRIPCIÓN
    // ----------------------------------------------------------------------
    public InscripcionTorneo rechazar(String idInscripcion) {
        InscripcionTorneo i = inscripcionRepo.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        i.setEstado("RECHAZADO");
        return inscripcionRepo.save(i);
    }

    // ----------------------------------------------------------------------
    // LISTAR INSCRITOS DE UN TORNEO
    // ----------------------------------------------------------------------
    public List<?> listarInscritos(String idTorneo) {

        return inscripcionRepo
                .findByCategoriaTorneoTorneoIdTorneo(idTorneo)
                .stream()
                .map(ins -> new Object() {
                    public final String idCompetidor =
                            ins.getRobot().getCompetidor().getIdCompetidor();
                    public final String competidor =
                            ins.getRobot().getCompetidor().getUsuario().getCorreo();
                    public final String robot =
                            ins.getRobot().getNombre();
                    public final String categoria =
                            ins.getCategoriaTorneo().getCategoria().name();
                    public final String estado =
                            ins.getEstado();
                })
                .collect(Collectors.toList());
    }




}
