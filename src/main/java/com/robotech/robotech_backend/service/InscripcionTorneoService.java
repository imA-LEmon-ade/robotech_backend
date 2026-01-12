package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.InscripcionIndividualDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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

        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        CategoriaTorneo categoria = categoriaRepo.findById(dto.getIdCategoriaTorneo())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (categoria.getModalidad() != ModalidadCategoria.INDIVIDUAL) {
            throw new RuntimeException("La categoría no es individual");
        }

        Robot robot = robotRepo.findById(dto.getIdRobot())
                .orElseThrow(() -> new RuntimeException("Robot no encontrado"));

        // 🔒 Validar que el robot pertenece al club
        if (!robot.getCompetidor().getClubActual().getIdClub()
                .equals(club.getIdClub())) {
            throw new RuntimeException("El robot no pertenece a este club");
        }

        // 🔒 Validar duplicado
        boolean yaInscrito =
                inscripcionRepo.existsByRobot_IdRobotAndCategoriaTorneo_Torneo_IdTorneoAndEstado(
                        robot.getIdRobot(),
                        categoria.getTorneo().getIdTorneo(),
                        EstadoInscripcion.ACTIVA
                );


        if (yaInscrito) {
            throw new RuntimeException("El robot ya está inscrito");
        }

        // 🔒 Validar cupos
        long inscritos = inscripcionRepo
                .countByCategoriaTorneoIdCategoriaTorneoAndEstado(
                        categoria.getIdCategoriaTorneo(),
                        EstadoInscripcion.ACTIVA
                );

        if (inscritos >= categoria.getMaxParticipantes()) {
            throw new RuntimeException("No hay cupos disponibles");
        }

        InscripcionTorneo inscripcion = InscripcionTorneo.builder()
                .categoriaTorneo(categoria)
                .robot(robot)
                .estado(EstadoInscripcion.ACTIVA)
                .build();

        inscripcionRepo.save(inscripcion);

        // 🔒 Cerrar inscripciones si se llenó
        if (inscritos + 1 >= categoria.getMaxParticipantes()) {
            categoria.setInscripcionesCerradas(true);
            categoriaRepo.save(categoria);
        }

        return inscripcion;
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
        inscripcion.setMotivoAnulacion(motivo);

        return inscripcionRepo.save(inscripcion);
    }
}
