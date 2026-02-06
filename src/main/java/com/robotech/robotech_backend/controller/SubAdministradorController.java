package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CambiarEstadoSubAdminDTO;
import com.robotech.robotech_backend.dto.CrearSubAdminDTO;
import com.robotech.robotech_backend.dto.EditarSubAdminDTO;
import com.robotech.robotech_backend.dto.PageResponse;
import com.robotech.robotech_backend.dto.SubAdminResponseDTO;
import com.robotech.robotech_backend.model.enums.EstadoSubAdmin;
import com.robotech.robotech_backend.service.SubAdministradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PageResponse<SubAdminResponseDTO>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by("idUsuario").ascending()
        );

        Page<SubAdminResponseDTO> result = subAdminService.listarTodos(pageable, q);
        return ResponseEntity.ok(new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        ));
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


