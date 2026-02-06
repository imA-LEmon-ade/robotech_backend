package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CompetidorPublicoDTO;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Robot;
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
    private final RobotRepository robotRepo;

    public List<CompetidorPublicoDTO> obtenerRanking() {
        List<Competidor> competidores = competidorRepo.findAll();

        // Convertimos a DTO con lógica de robots incluida
        List<CompetidorPublicoDTO> dtos = competidores.stream().map(comp -> {

            // 1. Obtener los objetos Robot reales para extraer sus nombres
            List<Robot> robotsDelCompetidor = robotRepo.findByCompetidor_IdCompetidor(comp.getIdCompetidor());

            // 2. Mapear solo los nombres a una lista de Strings para el modal
            List<String> nombresRobots = robotsDelCompetidor.stream()
                    .map(Robot::getNombre)
                    .collect(Collectors.toList());

            // 3. Obtener Nombre Completo desde la relación con Usuario
            String nombre = comp.getUsuario().getNombres() + " " + comp.getUsuario().getApellidos();

            // 4. Nombre del Club con validación de nulos
            String club = (comp.getClubActual() != null) ? comp.getClubActual().getNombre() : "Agente Libre";

            // 5. Puntos (Aquí podrías sumar puntos de sus robots si fuera necesario)
            int puntos = 0;

            // Creamos el DTO usando el constructor que incluye la lista de nombres
            return new CompetidorPublicoDTO(
                    nombre,
                    club,
                    nombresRobots, // ✅ Se añade la lista de nombres aquí
                    robotsDelCompetidor.size(),
                    puntos,
                    0
            );
        }).collect(Collectors.toList());

        // 6. Ordenar por Puntos (De mayor a menor)
        dtos.sort(Comparator.comparingInt(CompetidorPublicoDTO::getPuntosRanking).reversed());

        // 7. Asignar la posición del ranking (1, 2, 3...)
        for (int i = 0; i < dtos.size(); i++) {
            dtos.get(i).setRanking(i + 1);
        }

        return dtos;
    }
}

