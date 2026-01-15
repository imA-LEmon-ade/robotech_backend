package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.ClubResponseDTO;
import com.robotech.robotech_backend.dto.CrearClubDTO;
import com.robotech.robotech_backend.dto.EditarClubDTO;
import com.robotech.robotech_backend.service.AdminClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<ClubResponseDTO> listar(
            @RequestParam(required = false) String nombre
    ) {
        return adminClubService.listar(nombre);
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
