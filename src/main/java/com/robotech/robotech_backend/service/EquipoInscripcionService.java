package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.EquipoInscripcionDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipoInscripcionService {

    private final CategoriaTorneoRepository categoriaRepo;
    private final RobotRepository robotRepo;
    private final EquipoTorneoRepository equipoRepo;
    private final ClubRepository clubRepo;

    public EquipoTorneo inscribirEquipo(
            String idClubSesion,
            EquipoInscripcionDTO dto
    ) {

        Club club = clubRepo.findById(idClubSesion)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        CategoriaTorneo categoria = categoriaRepo.findById(dto.getIdCategoriaTorneo())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Torneo torneo = categoria.getTorneo();

        // 1️⃣ Validar modalidad
        if (categoria.getModalidad() != ModalidadCategoria.EQUIPO) {
            throw new RuntimeException(
                    "La modalidad de esta categoría es " + categoria.getModalidad()
            );
        }

        // 2️⃣ Validar fechas
        Date hoy = new Date();
        if (hoy.before(torneo.getFechaAperturaInscripcion()) ||
                hoy.after(torneo.getFechaCierreInscripcion()) ||
                Boolean.TRUE.equals(categoria.getInscripcionesCerradas())) {
            if (hoy.after(torneo.getFechaCierreInscripcion())) {
                categoria.setInscripcionesCerradas(true);
                categoriaRepo.save(categoria);
            }
            throw new RuntimeException("Inscripciones cerradas");
        }

        // 3️⃣ Validar nombre del equipo
        if (dto.getNombreEquipo() == null || dto.getNombreEquipo().isBlank()) {
            throw new RuntimeException("El nombre del equipo es obligatorio");
        }

        if (dto.getNombreEquipo().length() > 50) {
            throw new RuntimeException("El nombre del equipo es demasiado largo");
        }

        boolean nombreRepetido = equipoRepo
                .existsByNombreIgnoreCaseAndCategoriaTorneoIdCategoriaTorneo(
                        dto.getNombreEquipo(),
                        categoria.getIdCategoriaTorneo()
                );

        if (nombreRepetido) {
            throw new RuntimeException(
                    "Ya existe un equipo con ese nombre en esta categoría"
            );
        }

        // 4️⃣ Validar robots
        if (dto.getRobots() == null || dto.getRobots().isEmpty()) {
            throw new RuntimeException("Debe seleccionar al menos un robot");
        }

        if (dto.getRobots().size() > categoria.getMaxIntegrantesEquipo()) {
            throw new RuntimeException("Excede el límite de integrantes por equipo");
        }

        List<Robot> robots = dto.getRobots().stream()
                .map(id -> robotRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Robot no encontrado: " + id)))
                .toList();

        for (Robot r : robots) {

            if (!r.getCompetidor().getClubActual().getIdClub().equals(club.getIdClub())) {
                throw new RuntimeException(
                        "Robot " + r.getNombre() + " no pertenece al club");
            }

            boolean yaInscrito = equipoRepo
                    .existsByRobotsIdRobotAndCategoriaTorneoTorneoIdTorneoAndEstadoNot(
                            r.getIdRobot(),
                            torneo.getIdTorneo(),
                            EstadoEquipoTorneo.ANULADA
                    );

            if (yaInscrito) {
                throw new RuntimeException(
                        "Robot " + r.getNombre() + " ya está inscrito en este torneo");
            }
        }

        // 5️⃣ Validar cupos
        long equiposInscritos = equipoRepo
                .countByCategoriaTorneoIdCategoriaTorneoAndEstadoNot(
                        categoria.getIdCategoriaTorneo(),
                        EstadoEquipoTorneo.ANULADA
                );

        if (equiposInscritos >= categoria.getMaxEquipos()) {
            throw new RuntimeException("Categoría sin cupos disponibles");
        }

        // 6️⃣ Crear equipo
        EquipoTorneo equipo = EquipoTorneo.builder()
                .nombre(dto.getNombreEquipo())
                .club(club)
                .categoriaTorneo(categoria)
                .robots(robots)
                .estado(EstadoEquipoTorneo.ACTIVADA)
                .build();

        return equipoRepo.save(equipo);
    }

    public EquipoTorneo inscribirEquipoPorUsuario(
            String idUsuario,
            EquipoInscripcionDTO dto
    ) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        return inscribirEquipo(club.getIdClub(), dto);
    }

    public EquipoTorneo anularEquipo(String idEquipo, String motivo) {
        EquipoTorneo equipo = equipoRepo.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        if (equipo.getEstado() == EstadoEquipoTorneo.ANULADA) {
            throw new RuntimeException("La inscripción ya está anulada");
        }

        equipo.setEstado(EstadoEquipoTorneo.ANULADA);
        equipo.setMotivoAnulacion(motivo);
        equipo.setAnuladaEn(new Date());

        return equipoRepo.save(equipo);
    }
}


