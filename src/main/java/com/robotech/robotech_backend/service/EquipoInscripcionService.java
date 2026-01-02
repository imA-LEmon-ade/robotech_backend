package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.EquipoInscripcionDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipoInscripcionService {

    private final CategoriaTorneoRepository categoriaRepo;
    private final RobotRepository robotRepo;
    private final EquipoTorneoRepository equipoRepo;
    private final ClubRepository clubRepo;

    // ---------------------------------------------------------
    // INSCRIPCIÓN DE EQUIPO A CATEGORÍA (MODALIDAD EQUIPO)
    // ---------------------------------------------------------
    public EquipoTorneo inscribirEquipo(
            String idClubSesion,
            EquipoInscripcionDTO dto
    ) {

        Club club = clubRepo.findById(idClubSesion)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        CategoriaTorneo categoria = categoriaRepo.findById(dto.getIdCategoriaTorneo())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Torneo torneo = categoria.getTorneo();

        // 1️⃣ Validar modalidad de la categoría
         if (categoria.getModalidad() != ModalidadCategoria.EQUIPO) {
            throw new RuntimeException(
                    "La modalidad de esta categoría es " + categoria.getModalidad()
            );
         }

        // 2️⃣ Validar fechas de inscripción
        Date hoy = new Date();
        if (hoy.before(torneo.getFechaAperturaInscripcion()) ||
                hoy.after(torneo.getFechaCierreInscripcion())) {
            throw new RuntimeException("Inscripciones cerradas");
        }

        // 3️⃣ Validar cantidad de robots
        if (dto.getRobots() == null || dto.getRobots().isEmpty()) {
            throw new RuntimeException("Debe seleccionar al menos un robot");
        }

        if (dto.getRobots().size() > categoria.getMaxIntegrantesEquipo()) {
            throw new RuntimeException(
                    "Excede el límite de integrantes por equipo"
            );
        }

        // 4️⃣ Obtener robots y validar pertenencia + duplicados
        List<Robot> robots = dto.getRobots().stream()
                .map(id -> robotRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Robot no encontrado: " + id)))
                .toList();

        for (Robot r : robots) {

            if (!r.getCompetidor().getClub().equals(club)) {
                throw new RuntimeException(
                        "Robot " + r.getNombre() + " no pertenece al club");
            }

            boolean yaInscrito = equipoRepo
                    .existsByRobotsIdRobotAndCategoriaTorneoTorneoIdTorneo(
                            r.getIdRobot(),
                            torneo.getIdTorneo()
                    );

            if (yaInscrito) {
                throw new RuntimeException(
                        "Robot " + r.getNombre() + " ya está inscrito en este torneo");
            }
        }

        // 5️⃣ Validar cupo de equipos
        long equiposInscritos = equipoRepo
                .countByCategoriaTorneoIdCategoriaTorneo(
                        categoria.getIdCategoriaTorneo()
                );

        Integer maxEquipos = categoria.getMaxEquipos();

        if (maxEquipos == null) {
            throw new RuntimeException(
                    "La categoría no tiene definido el máximo de equipos"
            );
        }

        if (equiposInscritos >= maxEquipos) {
            throw new RuntimeException("Categoría sin cupos disponibles");
        }


        // 6️⃣ Crear equipo
        EquipoTorneo equipo = EquipoTorneo.builder()
                .club(club)
                .categoriaTorneo(categoria)
                .robots(robots)
                .estado("PENDIENTE")
                .build();

        return equipoRepo.save(equipo);
    }

    // ---------------------------------------------------------
    // INSCRIPCIÓN DE EQUIPO USANDO USUARIO AUTENTICADO
    // ---------------------------------------------------------
    public EquipoTorneo inscribirEquipoPorUsuario(
            String idUsuario,
            EquipoInscripcionDTO dto
    ) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        return inscribirEquipo(club.getIdClub(), dto);
    }
}