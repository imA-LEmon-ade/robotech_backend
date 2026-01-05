package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.FiltroRobotsAdminDTO;
import com.robotech.robotech_backend.dto.RobotAdminDTO;
import com.robotech.robotech_backend.model.CategoriaCompetencia;
import com.robotech.robotech_backend.model.Robot;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
                        .categoria(r.getCategoria().name()) // Enum → String
                        .competidor(r.getCompetidor().getNombres())
                        .club(r.getCompetidor().getClub().getNombre())
                        .build()
                )
                .toList();
    }

}
