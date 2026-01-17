package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.InscripcionIndividualDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class InscripcionTorneoService {

    private final CategoriaTorneoRepository categoriaRepo;
    private final RobotRepository robotRepo;
    private final InscripcionTorneoRepository inscripcionRepo;
    private final ClubRepository clubRepo;

    @Transactional
    public InscripcionTorneo inscribirIndividualComoClub(
            String idUsuarioClub,
            InscripcionIndividualDTO dto
    ) {
        // 1. Recuperar entidades con sus relaciones cargadas
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        CategoriaTorneo categoria = categoriaRepo.findById(dto.getIdCategoriaTorneo())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (categoria.getModalidad() != ModalidadCategoria.INDIVIDUAL) {
            throw new RuntimeException("La categoría no es individual");
        }

        Robot robot = robotRepo.findById(dto.getIdRobot())
                .orElseThrow(() -> new RuntimeException("Robot no encontrado"));

        // 2. Validaciones de seguridad
        if (robot.getCompetidor() == null || robot.getCompetidor().getClubActual() == null ||
                !robot.getCompetidor().getClubActual().getIdClub().equals(club.getIdClub())) {
            throw new RuntimeException("El robot no pertenece a este club");
        }

        // 3. Validar duplicados y cupos
        boolean yaInscrito = inscripcionRepo.existsByRobot_IdRobotAndCategoriaTorneo_Torneo_IdTorneoAndEstado(
                robot.getIdRobot(),
                categoria.getTorneo().getIdTorneo(),
                EstadoInscripcion.ACTIVA
        );

        if (yaInscrito) {
            throw new RuntimeException("El robot ya está inscrito en este torneo");
        }

        long inscritos = inscripcionRepo.countByCategoriaTorneoIdCategoriaTorneoAndEstado(
                categoria.getIdCategoriaTorneo(),
                EstadoInscripcion.ACTIVA
        );

        if (inscritos >= categoria.getMaxParticipantes()) {
            throw new RuntimeException("No hay cupos disponibles");
        }

        // 4. CREACIÓN Y GUARDADO (Ajuste para asegurar mapeo correcto)
        InscripcionTorneo inscripcion = new InscripcionTorneo();
        inscripcion.setCategoriaTorneo(categoria);
        inscripcion.setRobot(robot);
        inscripcion.setEstado(EstadoInscripcion.ACTIVA);
        inscripcion.setFechaInscripcion(new Date());

        // Guardamos explícitamente
        InscripcionTorneo guardada = inscripcionRepo.save(inscripcion);

        // 5. Gestión de cupos
        if (inscritos + 1 >= categoria.getMaxParticipantes()) {
            categoria.setInscripcionesCerradas(true);
            categoriaRepo.save(categoria);
        }

        return guardada;
    }

    @Transactional
    public InscripcionTorneo anularInscripcion(String idInscripcion, String motivo) {
        InscripcionTorneo inscripcion = inscripcionRepo.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        if (inscripcion.getEstado() == EstadoInscripcion.ANULADA) {
            throw new RuntimeException("La inscripción ya está anulada");
        }

        inscripcion.setEstado(EstadoInscripcion.ANULADA);
        inscripcion.setMotivoAnulacion(motivo);
        // Sugerencia: Actualizar fecha de anulación si existe el campo
        // inscripcion.setAnuladaEn(new Date());

        return inscripcionRepo.save(inscripcion);
    }
}