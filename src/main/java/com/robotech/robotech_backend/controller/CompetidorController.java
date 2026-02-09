package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CompetidorActualizarDTO;
import com.robotech.robotech_backend.dto.CompetidorPerfilDTO;
import com.robotech.robotech_backend.service.CompetidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/competidores")
@CrossOrigin(origins = "${app.frontend.url}")
public class CompetidorController {

    @Autowired
    private CompetidorService competidorService;

    // -------------------------------------------------------------------
    // 1. LISTAR COMPETIDORES (Con filtro de búsqueda opcional)
    // -------------------------------------------------------------------
    // El frontend envía: GET /api/competidores/club/123?busqueda=juan
    @GetMapping("/club/{idClub}")
    public ResponseEntity<?> listarPorClub(
            @PathVariable String idClub,
            @RequestParam(required = false) String busqueda, // ✨ NUEVO: Recibe el filtro
            @RequestParam(required = false, defaultValue = "false") boolean excluirPropietario
    ) {
        // Llamamos al método sobrecargado del servicio
        return ResponseEntity.ok(
                competidorService.listarPorClub(idClub, busqueda, excluirPropietario)
        );
    }

    // -------------------------------------------------------------------
    // 2. APROBAR (Lógica delegada al servicio)
    // -------------------------------------------------------------------
    @PutMapping("/{idCompetidor}/aprobar")
    public ResponseEntity<?> aprobarCompetidor(@PathVariable String idCompetidor) {
        try {
            competidorService.aprobarCompetidor(idCompetidor);
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "Competidor aprobado correctamente"));
        } catch (RuntimeException e) {
            // Devolvemos el error del servicio (ej: "Ya estaba aprobado")
            return ResponseEntity.badRequest().body(Collections.singletonMap("mensaje", e.getMessage()));
        }
    }

    // -------------------------------------------------------------------
    // 3. RECHAZAR (Lógica delegada al servicio)
    // -------------------------------------------------------------------
    @PutMapping("/{idCompetidor}/rechazar")
    public ResponseEntity<?> rechazarCompetidor(@PathVariable String idCompetidor) {
        try {
            competidorService.rechazarCompetidor(idCompetidor);
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "Competidor rechazado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("mensaje", e.getMessage()));
        }
    }

    // -------------------------------------------------------------------
    // 4. OTROS MÉTODOS (Intactos, solo limpieza)
    // -------------------------------------------------------------------

    @GetMapping("/{idCompetidor}")
    public ResponseEntity<CompetidorPerfilDTO> obtenerPerfil(@PathVariable String idCompetidor) {
        return ResponseEntity.ok(competidorService.obtenerPerfil(idCompetidor));
    }

    @PostMapping("/{idCompetidor}/foto")
    public ResponseEntity<?> subirFoto(
            @PathVariable String idCompetidor,
            @RequestParam("foto") MultipartFile foto
    ) {
        String url = competidorService.subirFoto(idCompetidor, foto);
        return ResponseEntity.ok(Map.of("fotoUrl", url));
    }

    @PutMapping("/{idCompetidor}")
    public ResponseEntity<?> actualizarPerfil(
            @PathVariable String idCompetidor,
            @RequestBody CompetidorActualizarDTO dto
    ) {
        competidorService.actualizarPerfil(idCompetidor, dto);
        return ResponseEntity.ok().build();
    }
}

