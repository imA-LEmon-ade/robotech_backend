package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CompetidorPublicoDTO;
import com.robotech.robotech_backend.dto.PageResponse;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Robot;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompetidorPublicoService {

    private final CompetidorRepository competidorRepo;
    private final RobotRepository robotRepo;

    public PageResponse<CompetidorPublicoDTO> obtenerRanking(Pageable pageable, String q) {
        Page<Competidor> page = competidorRepo.buscarPublico(q, pageable);
        List<Competidor> competidores = page.getContent();

        List<String> ids = competidores.stream()
                .map(Competidor::getIdCompetidor)
                .toList();

        Map<String, List<Robot>> robotsPorCompetidor = new HashMap<>();
        if (!ids.isEmpty()) {
            List<Robot> robots = robotRepo.findByCompetidor_IdCompetidorIn(ids);
            for (Robot r : robots) {
                if (r.getCompetidor() == null) continue;
                robotsPorCompetidor
                        .computeIfAbsent(r.getCompetidor().getIdCompetidor(), k -> new ArrayList<>())
                        .add(r);
            }
        }

        List<CompetidorPublicoDTO> dtos = competidores.stream().map(comp -> {
            List<Robot> robotsDelCompetidor = robotsPorCompetidor.getOrDefault(comp.getIdCompetidor(), List.of());
            List<String> nombresRobots = robotsDelCompetidor.stream()
                    .map(Robot::getNombre)
                    .collect(Collectors.toList());

            String nombre = comp.getUsuario().getNombres() + " " + comp.getUsuario().getApellidos();
            String club = (comp.getClubActual() != null) ? comp.getClubActual().getNombre() : "Agente Libre";
            int puntos = 0;

            return new CompetidorPublicoDTO(
                    nombre,
                    club,
                    nombresRobots,
                    robotsDelCompetidor.size(),
                    puntos,
                    0
            );
        }).collect(Collectors.toList());

        dtos.sort(Comparator.comparingInt(CompetidorPublicoDTO::getPuntosRanking).reversed());

        int start = page.getNumber() * page.getSize();
        for (int i = 0; i < dtos.size(); i++) {
            dtos.get(i).setRanking(start + i + 1);
        }

        return new PageResponse<>(
                dtos,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
