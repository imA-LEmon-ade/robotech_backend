package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RobotPublicoDTO;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RobotPublicoService {

    private final RobotRepository robotRepo;

    public List<RobotPublicoDTO> obtenerRobotsPublicos() {
        return robotRepo.findAll().stream().map(r -> new RobotPublicoDTO(
                r.getIdRobot(),
                r.getNombre(),
                r.getCategoria() != null ? r.getCategoria().name() : "Sin Categoría",
                r.getNickname(),
                r.getCompetidor().getUsuario().getNombres(),
                r.getCompetidor().getClubActual() != null ? r.getCompetidor().getClubActual().getNombre() : "Independiente"
        )).toList();
    }
}