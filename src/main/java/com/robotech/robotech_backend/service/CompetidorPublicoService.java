package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CompetidorPublicoDTO;
import com.robotech.robotech_backend.model.Competidor;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class CompetidorPublicoService {

    private final CompetidorRepository competidorRepo;
    private final RobotRepository robotRepo; // Para contar sus robots

    public List<CompetidorPublicoDTO> obtenerRanking() {
        List<Competidor> competidores = competidorRepo.findAll();

        // Convertimos a DTO
        List<CompetidorPublicoDTO> dtos = competidores.stream().map(comp -> {

            // 1. Contar robots reales de este competidor
            int cantidadRobots = robotRepo.countByCompetidor_IdCompetidor(comp.getIdCompetidor());

            // 2. Obtener Nombre Completo (Seguro)
            String nombre = comp.getUsuario().getNombres() + " " + comp.getUsuario().getApellidos();
            // O si usas Usuario: comp.getUsuario().getNombre();

            // 3. Nombre del Club
            String club = (comp.getClubActual() != null) ? comp.getClubActual().getNombre() : "Agente Libre";

            // 4. Puntos (Si aún no tienes lógica, pon 0 o un random para probar)
            int puntos = 0; // Aquí iría comp.getPuntosAcumulados();

            return new CompetidorPublicoDTO(nombre, club, cantidadRobots, puntos, 0);
        }).collect(Collectors.toList());

        // 5. Ordenar por Puntos (De mayor a menor) para simular Ranking real
        dtos.sort(Comparator.comparingInt(CompetidorPublicoDTO::getPuntosRanking).reversed());

        // 6. Asignar la posición del ranking (1, 2, 3...)
        for (int i = 0; i < dtos.size(); i++) {
            dtos.get(i).setRanking(i + 1);
        }

        return dtos;
    }
}