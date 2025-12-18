package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CrearClubDTO;
import com.robotech.robotech_backend.dto.EditarClubDTO;
import com.robotech.robotech_backend.service.AdminClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/clubes")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminClubController {

    private final AdminClubService clubService;

    // -------------------------------------------------------
    // 🟦 CREAR CLUB
    // -------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> crearClub(@RequestBody CrearClubDTO dto) {
        try {
            return ResponseEntity.ok(clubService.crearClub(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // -------------------------------------------------------
    // 🟦 LISTAR con búsqueda opcional
    // GET /api/admin/clubes?nombre=robot
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String nombre) {
        try {
            return ResponseEntity.ok(clubService.listar(nombre));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // -------------------------------------------------------
    // 🟦 EDITAR CLUB
    // -------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable String id,
                                    @RequestBody EditarClubDTO dto) {
        return ResponseEntity.ok(clubService.editar(id, dto));
    }


    // -------------------------------------------------------
    // 🟦 ELIMINAR CLUB
    // -------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            clubService.eliminar(id);
            return ResponseEntity.ok("Club eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

