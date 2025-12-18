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

    public EquipoTorneo inscribirEquipo(
            String idClubSesion,
            EquipoInscripcionDTO dto
    ) {

        Club club = clubRepo.findById(idClubSesion)
                .orElseThrow(() ->
                        new RuntimeException("Club no encontrado"));

        CategoriaTorneo categoria = categoriaRepo
                .findById(dto.getIdCategoriaTorneo())
                .orElseThrow(() ->
                        new RuntimeException("Categoría no encontrada"));

        Torneo torneo = categoria.getTorneo();

        // 1️⃣ Validar tipo torneo
        if (!torneo.getTipo().equals("EQUIPOS")) {
            throw new RuntimeException("Este torneo no es por equipos");
        }

        // 2️⃣ Validar fechas
        Date hoy = new Date();
        if (hoy.before(torneo.getFechaAperturaInscripcion()) ||
                hoy.after(torneo.getFechaCierreInscripcion())) {
            throw new RuntimeException("Inscripciones cerradas");
        }

        // 3️⃣ Validar límite de integrantes
        if (dto.getRobots().size() > categoria.getMaxIntegrantesEquipo()) {
            throw new RuntimeException(
                    "Excede el límite de integrantes por equipo");
        }

        // 4️⃣ Obtener robots y validar pertenencia
        List<Robot> robots = dto.getRobots().stream()
                .map(id -> robotRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Robot no encontrado")))
                .toList();

        for (Robot r : robots) {
            if (!r.getCompetidor().getClub().equals(club)) {
                throw new RuntimeException(
                        "Robot no pertenece al club");
            }

            boolean yaInscrito = equipoRepo
                    .existsByRobotsIdRobotAndCategoriaTorneoTorneoIdTorneo(
                            r.getIdRobot(),
                            torneo.getIdTorneo()
                    );

            if (yaInscrito) {
                throw new RuntimeException(
                        "Robot ya inscrito en este torneo");
            }
        }

        // 5️⃣ Validar cupo de equipos
        long equiposInscritos = equipoRepo
                .countByCategoriaTorneoIdCategoriaTorneo(
                        categoria.getIdCategoriaTorneo());

        if (equiposInscritos >= categoria.getMaxParticipantes()) {
            throw new RuntimeException("Categoría sin cupos");
        }

        // 6️⃣ Crear equipo
        EquipoTorneo equipo = EquipoTorneo.builder()
                .club(club)
                .categoriaTorneo(categoria)
                .robots(robots)
                .build();

        return equipoRepo.save(equipo);
    }
}
