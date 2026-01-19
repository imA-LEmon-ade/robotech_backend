package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CambiarEstadoSubAdminDTO;
import com.robotech.robotech_backend.dto.CrearSubAdminDTO;
import com.robotech.robotech_backend.dto.EditarSubAdminDTO;
import com.robotech.robotech_backend.dto.SubAdminResponseDTO;
import com.robotech.robotech_backend.model.EstadoSubAdmin;
import com.robotech.robotech_backend.service.SubAdministradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/subadmins")
@RequiredArgsConstructor
public class SubAdministradorController {

    private final SubAdministradorService subAdminService;

    // CREAR
    @PostMapping
    public ResponseEntity<SubAdminResponseDTO> crear(
            @RequestBody @Valid CrearSubAdminDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subAdminService.crear(dto));
    }

    // LISTAR
    @GetMapping
    public ResponseEntity<List<SubAdminResponseDTO>> listarTodos() {
        return ResponseEntity.ok(subAdminService.listarTodos());
    }

    // CAMBIAR ESTADO ✅
    @PutMapping("/{id}/estado")
    public SubAdminResponseDTO cambiarEstado(
            @PathVariable String id,
            @RequestBody CambiarEstadoSubAdminDTO dto
    ) {
        return subAdminService.cambiarEstado(id, dto.getEstado());
    }

    // EDITAR ✅
    @PutMapping("/{id}")
    public SubAdminResponseDTO editar(
            @PathVariable String id,
            @RequestBody EditarSubAdminDTO dto
    ) {
        return subAdminService.editar(id, dto);
    }
}
