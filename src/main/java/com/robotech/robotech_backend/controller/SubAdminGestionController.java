package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CompetidorResponseDTO;
import com.robotech.robotech_backend.dto.RegistroCompetidorDTO;
import com.robotech.robotech_backend.service.SubAdminCompetidorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subadmin")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url}") // Para permitir las peticiones de React
public class SubAdminGestionController {

    private final SubAdminCompetidorService competidorService;

    // REGISTRAR COMPETIDOR
    @PostMapping("/competidores")
    @PreAuthorize("hasAuthority('SUBADMINISTRADOR')")
    public ResponseEntity<String> registrarCompetidor(
            @RequestBody @Valid RegistroCompetidorDTO dto
    ) {
        competidorService.registrarCompetidor(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Competidor registrado exitosamente");
    }
    @GetMapping("/competidores")
    @PreAuthorize("hasAuthority('SUBADMINISTRADOR')")
    public ResponseEntity<List<CompetidorResponseDTO>> listarParaTabla() {
        return ResponseEntity.ok(competidorService.listarTodos());
    }
}

