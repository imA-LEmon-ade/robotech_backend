package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.InscripcionIndividualDTO;
import com.robotech.robotech_backend.dto.RobotDTO;
import com.robotech.robotech_backend.model.entity.CategoriaTorneo;
import com.robotech.robotech_backend.model.entity.Robot;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import com.robotech.robotech_backend.service.InscripcionTorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competidor/inscripciones")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasAuthority('COMPETIDOR')")
public class CompetidorInscripcionController {

    private final InscripcionTorneoService individualService;
    private final RobotRepository robotRepo;
    private final CompetidorRepository competidorRepo;
    private final CategoriaTorneoRepository categoriaRepo;

    @PostMapping("/individual")
    public ResponseEntity<?> inscribirIndividual(@RequestBody InscripcionIndividualDTO dto, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(individualService.inscribirIndividualComoCompetidor(usuario.getIdUsuario(), dto));
    }

    @GetMapping("/robots-disponibles/{idCategoriaTorneo}")
    public ResponseEntity<List<RobotDTO>> robotsDisponibles(@PathVariable String idCategoriaTorneo, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();

        com.robotech.robotech_backend.model.entity.Competidor competidor = competidorRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        CategoriaTorneo categoriaTorneo = categoriaRepo.findById(idCategoriaTorneo)
                .orElseThrow(() -> new RuntimeException("Categor?a no encontrada"));

        List<Robot> robots = robotRepo.findRobotsDisponiblesCompetidor(
                competidor.getIdCompetidor(),
                categoriaTorneo.getCategoria(),
                categoriaTorneo.getTorneo().getIdTorneo()
        );

        List<RobotDTO> dtos = robots.stream()
                .map(r -> new RobotDTO(
                        r.getNombre(),
                        r.getCategoria().name(),
                        r.getNickname(),
                        r.getIdRobot()
                ))
                .toList();

        return ResponseEntity.ok(dtos);
    }
}



