package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.InscripcionIndividualDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InscripcionTorneoService {

    private final CategoriaTorneoRepository categoriaRepo;
    private final RobotRepository robotRepo;
    private final InscripcionTorneoRepository inscripcionRepo;
    private final ClubRepository clubRepo;

    // ----------------------------------------------------------------------
    // INSCRIBIR ROBOT (MODALIDAD INDIVIDUAL - CLUB)
    // ----------------------------------------------------------------------
    @Transactional
    public InscripcionTorneo inscribirIndividualComoClub(
            String idUsuarioClub,
            InscripcionIndividualDTO dto
    ) {

        // 1) Club
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        // 2) Categoría del torneo
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
        if (!robot.getCompetidor()
                .getClubActual()
                .getIdClub()
                .equals(club.getIdClub())) {
            throw new RuntimeException("El robot no pertenece a este club");
        }

        // 7) Validar categoría del robot
        if (robot.getCategoria() != categoria.getCategoria()) {
            throw new RuntimeException(
                    "El robot es de categoría " + robot.getCategoria()
                            + " y la categoría del torneo es " + categoria.getCategoria()
            );
        }

        // 8) Validar estado del robot
        if (robot.getEstado() != EstadoRobot.ACTIVO) {
            throw new RuntimeException("Robot inactivo");
        }

        // 9) Validar duplicado (solo cuentan las ACTIVAS)
        boolean yaInscrito = inscripcionRepo
                .existsByRobotIdRobotAndCategoriaTorneoTorneoIdTorneoAndEstado(
                        robot.getIdRobot(),
                        torneo.getIdTorneo(),
                        EstadoInscripcion.ACTIVA
                );

        if (yaInscrito) {
            throw new RuntimeException("El robot ya está inscrito en este torneo");
        }

        // 10) Validar cupos (solo ACTIVAS)
        long inscritos = inscripcionRepo
                .countByCategoriaTorneoIdCategoriaTorneoAndEstado(
                        categoria.getIdCategoriaTorneo(),
                        EstadoInscripcion.ACTIVA
                );

        if (inscritos >= categoria.getMaxParticipantes()) {
            throw new RuntimeException("No hay cupos disponibles en esta categoría");
        }

        // 11) Crear inscripción ACTIVA
        InscripcionTorneo inscripcion = InscripcionTorneo.builder()
                .categoriaTorneo(categoria)
                .robot(robot)
                .estado(EstadoInscripcion.ACTIVA)
                .fechaInscripcion(new Date())
                .build();

        return inscripcionRepo.save(inscripcion);
    }

    // ----------------------------------------------------------------------
    // ANULAR INSCRIPCIÓN (ADMIN)
    // ----------------------------------------------------------------------
    @Transactional
    public InscripcionTorneo anularInscripcion(
            String idInscripcion,
            String motivo
    ) {

        InscripcionTorneo inscripcion = inscripcionRepo.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        if (inscripcion.getEstado() == EstadoInscripcion.ANULADA) {
            throw new RuntimeException("La inscripción ya está anulada");
        }

        inscripcion.setEstado(EstadoInscripcion.ANULADA);
        inscripcion.setMotivoAnulacion(motivo); // si tenés el campo

        return inscripcionRepo.save(inscripcion);
    }

    // ----------------------------------------------------------------------
    // LISTAR INSCRITOS DE UN TORNEO (SOLO ACTIVOS)
    // ----------------------------------------------------------------------
    public List<InscripcionTorneo> listarInscritosActivos(String idTorneo) {

        return inscripcionRepo
                .findByCategoriaTorneoTorneoIdTorneo(idTorneo)
                .stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                .toList();
    }

    @Transactional
    public void anular(String id) {
        InscripcionTorneo inscripcion = inscripcionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("No encontrada"));

        if (inscripcion.getEstado() == EstadoInscripcion.ANULADA) return;

        inscripcion.setEstado(EstadoInscripcion.ANULADA);
    }
}
