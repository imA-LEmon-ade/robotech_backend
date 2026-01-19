package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RobotAdminDTO;
import com.robotech.robotech_backend.model.CategoriaCompetencia;
import com.robotech.robotech_backend.model.Robot;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminRobotService {

    private final RobotRepository robotRepo;

    public List<RobotAdminDTO> listarRobots(
            String nombre,
            String categoria,
            String idClub
    ) {

        CategoriaCompetencia categoriaEnum = null;

        if (categoria != null && !categoria.isBlank()) {
            categoriaEnum = CategoriaCompetencia.valueOf(categoria);
        }

        List<Robot> robots = robotRepo.filtrarRobots(
                nombre,
                categoriaEnum,
                idClub
        );

        return robots.stream()
                .map(r -> RobotAdminDTO.builder()
                        .idRobot(r.getIdRobot())
                        .nombre(r.getNombre())
                        .nickname(r.getNickname())
                        .categoria(r.getCategoria().name())

                        // COMPETIDOR → usamos DNI (modelo final)
                        .competidor(
                                Optional.ofNullable(r.getCompetidor())
                                        .map(c -> c.getUsuario().getDni())
                                        .orElse(null)
                        )

                        // CLUB → clubActual
                        .club(
                                Optional.ofNullable(r.getCompetidor())
                                        .map(c -> c.getClubActual())
                                        .map(cl -> cl.getNombre())
                                        .orElse(null)
                        )
                        .build()
                )
                .toList();
    }
}
