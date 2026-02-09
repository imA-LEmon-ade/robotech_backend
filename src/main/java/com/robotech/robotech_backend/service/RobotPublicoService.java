package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.PageResponse;
import com.robotech.robotech_backend.dto.RobotPublicoDTO;
import com.robotech.robotech_backend.model.entity.Robot;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RobotPublicoService {

    private final RobotRepository robotRepo;

    public PageResponse<RobotPublicoDTO> obtenerRobotsPublicos(Pageable pageable, String q) {
        Page<Robot> page = robotRepo.buscarPublico(q, pageable);
        List<RobotPublicoDTO> content = page.getContent().stream().map(r -> {
            String nombreCompleto = (r.getCompetidor() != null && r.getCompetidor().getUsuario() != null)
                    ? r.getCompetidor().getUsuario().getNombres() + " " + r.getCompetidor().getUsuario().getApellidos()
                    : "Independiente";

            return new RobotPublicoDTO(
                    r.getIdRobot(),
                    r.getNombre(),
                    r.getCategoria() != null ? r.getCategoria().name() : "Sin Categoria",
                    r.getNickname(),
                    nombreCompleto,
                    (r.getCompetidor() != null && r.getCompetidor().getClubActual() != null)
                            ? r.getCompetidor().getClubActual().getNombre()
                            : "Independiente"
            );
        }).toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
