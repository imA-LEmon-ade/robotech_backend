package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.ClubResponseDTO;
import com.robotech.robotech_backend.dto.CrearClubDTO;
import com.robotech.robotech_backend.dto.EditarClubDTO;
import com.robotech.robotech_backend.dto.PageResponse;
import com.robotech.robotech_backend.service.AdminClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/clubes")
@RequiredArgsConstructor
public class AdminClubController {

    private final AdminClubService adminClubService;

    @PostMapping
    public ResponseEntity<ClubResponseDTO> crearClub(
            @Valid @RequestBody CrearClubDTO dto
    ) {
        ClubResponseDTO res = adminClubService.crearClub(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    public PageResponse<ClubResponseDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String nombre
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by("idClub").ascending()
        );

        Page<ClubResponseDTO> result = adminClubService.listar(nombre, pageable);
        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @PutMapping("/{id}")
    public ClubResponseDTO editar(
            @PathVariable String id,
            @RequestBody EditarClubDTO dto
    ) {
        return adminClubService.editar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        adminClubService.eliminar(id);
    }
}


