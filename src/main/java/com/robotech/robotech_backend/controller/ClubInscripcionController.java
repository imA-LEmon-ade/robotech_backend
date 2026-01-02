package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.EquipoInscripcionDTO;
import com.robotech.robotech_backend.dto.InscripcionIndividualDTO;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.service.EquipoInscripcionService;
import com.robotech.robotech_backend.service.InscripcionTorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club/inscripciones")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ClubInscripcionController {

    private final InscripcionTorneoService individualService;
    private final EquipoInscripcionService equipoService;

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
}
