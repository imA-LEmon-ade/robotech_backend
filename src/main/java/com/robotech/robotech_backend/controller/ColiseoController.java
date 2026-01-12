package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.ColiseoDTO;
import com.robotech.robotech_backend.model.Coliseo;
import com.robotech.robotech_backend.repository.ColiseoRepository;
import com.robotech.robotech_backend.service.ColiseoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;


@RestController
@RequestMapping("/api/admin/coliseos")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ColiseoController {

    private final ColiseoService coliseoService;
    private final ColiseoRepository coliseoRepository;

    // -------------------------
    // CREAR
    // -------------------------
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ColiseoDTO dto) {
        return ResponseEntity.ok(coliseoService.crear(dto));
    }

    // -------------------------
    // LISTAR
    // -------------------------
    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(coliseoService.listar());
    }

    // -------------------------
    // EDITAR
    // -------------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable String id,
            @RequestBody ColiseoDTO dto
    ) {
        return ResponseEntity.ok(coliseoService.editar(id, dto));
    }

    // -------------------------
    // ELIMINAR
    // -------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        coliseoService.eliminar(id);
        return ResponseEntity.ok("Coliseo eliminado");
    }

    @PostMapping("/{id}/imagen")
    public ResponseEntity<?> subirImagen(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Coliseo c = coliseoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coliseo no encontrado"));

        String nombreArchivo = id + "_" + file.getOriginalFilename();
        Path ruta = Paths.get("uploads/coliseos/" + nombreArchivo);

        Files.createDirectories(ruta.getParent());
        Files.write(ruta, file.getBytes());

        c.setImagenUrl("/uploads/coliseos/" + nombreArchivo);
        coliseoRepository.save(c);

        return ResponseEntity.ok(c.getImagenUrl());
    }

}
