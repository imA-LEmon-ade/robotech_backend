package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.EquipoInscripcionDTO;
import com.robotech.robotech_backend.dto.InscripcionIndividualDTO;
import com.robotech.robotech_backend.dto.RobotDTO; // 👈 Importante
import com.robotech.robotech_backend.model.entity.CategoriaTorneo;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Robot;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import com.robotech.robotech_backend.service.EquipoInscripcionService;
import com.robotech.robotech_backend.service.InscripcionTorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/club/inscripciones")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ClubInscripcionController {

    private final InscripcionTorneoService individualService;
    private final EquipoInscripcionService equipoService;
    private final RobotRepository robotRepo;
    private final ClubRepository clubRepo;
    private final CategoriaTorneoRepository categoriaRepo;

    // ----------------------------------
    // INSCRIPCIÓN INDIVIDUAL
    // ----------------------------------
    @PostMapping("/individual")
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<?> inscribirIndividual(
            @RequestBody InscripcionIndividualDTO dto,
            Authentication auth
    ) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(
                individualService.inscribirIndividualComoClub(
                        usuario.getIdUsuario(),
                        dto
                )
        );
    }

    // ----------------------------------
    // INSCRIPCIÓN POR EQUIPO
    // ----------------------------------
    @PostMapping("/equipo")
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<?> inscribirEquipo(
            @RequestBody EquipoInscripcionDTO dto,
            Authentication auth
    ) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(
                equipoService.inscribirEquipoPorUsuario(
                        usuario.getIdUsuario(),
                        dto
                )
        );
    }

    // ----------------------------------
    // LISTAR ROBOTS PARA INSCRIPCIÓN
    // ----------------------------------
    @GetMapping("/robots-disponibles/{idCategoriaTorneo}")
    public ResponseEntity<List<RobotDTO>> robotsDisponibles( // 👈 Devuelve DTOs
                                                             @PathVariable String idCategoriaTorneo,
                                                             Authentication auth
    ) {
        Usuario usuario = (Usuario) auth.getPrincipal();

        Club club = clubRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        CategoriaTorneo categoriaTorneo = categoriaRepo.findById(idCategoriaTorneo)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Buscamos las entidades
        List<Robot> robots = robotRepo.findRobotsDisponibles(
                club.getIdClub(),
                categoriaTorneo.getCategoria(),
                categoriaTorneo.getTorneo().getIdTorneo()
        );

        // 👇 CONVERTIMOS A DTO PARA EVITAR ERROR DE HIBERNATE/BYTEBUDDY
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

