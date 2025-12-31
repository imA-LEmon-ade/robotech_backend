package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.InscripcionDTO;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.service.InscripcionTorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias-torneo")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InscripcionController {

    private final InscripcionTorneoService inscripcionService;

    // ------------------------------------------------------------------
    // INSCRIBIR ROBOT (MODALIDAD INDIVIDUAL)
    // ------------------------------------------------------------------
    @PostMapping("/{idCategoria}/inscribir")
    @PreAuthorize("hasAuthority('COMPETIDOR')")
    public ResponseEntity<?> inscribirIndividual(
            @PathVariable String idCategoria,
            @RequestBody InscripcionDTO dto,
            Authentication auth
    ) {

        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        return ResponseEntity.ok(
                inscripcionService.inscribirIndividual(
                        idCategoria,
                        dto.getIdRobot(),
                        usuario.getIdUsuario()
                )
        );
    }

    // ------------------------------------------------------------------
    // LISTAR INSCRITOS DE UN TORNEO
    // ------------------------------------------------------------------
    @GetMapping("/torneo/{idTorneo}/inscritos")
    public ResponseEntity<?> listarInscritos(@PathVariable String idTorneo) {
        return ResponseEntity.ok(
                inscripcionService.listarInscritos(idTorneo)
        );
    }
}
