package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.ActualizarEncuentroAdminDTO;
import com.robotech.robotech_backend.dto.CategoriaEncuentroAdminDTO;
import com.robotech.robotech_backend.dto.CrearEncuentrosDTO;
import com.robotech.robotech_backend.dto.EncuentroAdminDTO;
import com.robotech.robotech_backend.model.Encuentro;
import com.robotech.robotech_backend.service.AdminEncuentrosService;
import com.robotech.robotech_backend.service.EncuentroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/encuentros")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public class AdminEncuentrosController {

    private final AdminEncuentrosService adminEncuentrosService;
    private final EncuentroService encuentroService;

    // ----------------------------------------------------
    // 1️⃣ LISTAR CATEGORÍAS LISTAS PARA GENERAR ENCUENTROS
    // ----------------------------------------------------
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaEncuentroAdminDTO>> listarCategorias() {
        return ResponseEntity.ok(
                adminEncuentrosService.listarCategoriasActivas()
        );
    }

    // ----------------------------------------------------
    // 2️⃣ CREAR ENCUENTROS (ELIMINACIÓN / TODOS VS TODOS)
    // ----------------------------------------------------
    @PostMapping("/generar")
    public ResponseEntity<List<EncuentroAdminDTO>> generarEncuentros(
            @Valid @RequestBody CrearEncuentrosDTO dto
    ) {
        return ResponseEntity.ok(
                encuentroService.generarEncuentros(dto)
        );
    }

    // ----------------------------------------------------
    // 2.1 REGENERAR ENCUENTROS (BORRA Y CREA DE NUEVO)
    // ----------------------------------------------------
    @PostMapping("/regenerar")
    public ResponseEntity<List<EncuentroAdminDTO>> regenerarEncuentros(
            @Valid @RequestBody CrearEncuentrosDTO dto
    ) {
        return ResponseEntity.ok(
                encuentroService.regenerarEncuentros(dto)
        );
    }

    // ----------------------------------------------------
    // 3. LISTAR ENCUENTROS POR CATEGORIA
    // ----------------------------------------------------
    @GetMapping("/categoria/{idCategoriaTorneo}")
    public ResponseEntity<List<EncuentroAdminDTO>> listarPorCategoria(
            @PathVariable String idCategoriaTorneo
    ) {
        return ResponseEntity.ok(
                encuentroService.listarEncuentrosAdminPorCategoria(idCategoriaTorneo)
        );
    }

    // ----------------------------------------------------
    // 4. EDITAR ENCUENTRO
    // ----------------------------------------------------
    @PutMapping("/{idEncuentro}")
    public ResponseEntity<EncuentroAdminDTO> actualizar(
            @PathVariable String idEncuentro,
            @RequestBody ActualizarEncuentroAdminDTO dto
    ) {
        return ResponseEntity.ok(
                encuentroService.actualizarEncuentroAdmin(idEncuentro, dto)
        );
    }

}
