package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.ColiseoDTO;
import com.robotech.robotech_backend.dto.PageResponse;
import com.robotech.robotech_backend.model.entity.Coliseo;
import com.robotech.robotech_backend.repository.ColiseoRepository;
import com.robotech.robotech_backend.service.ColiseoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<PageResponse<ColiseoDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by("idColiseo").ascending()
        );

        Page<ColiseoDTO> result = coliseoService.listar(pageable, q);
        return ResponseEntity.ok(new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        ));
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


