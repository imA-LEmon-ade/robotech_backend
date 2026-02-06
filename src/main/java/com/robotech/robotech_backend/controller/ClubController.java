package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.ClubService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // ✅ Importante para mi-club
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/clubes")
@CrossOrigin(origins = "*")
public class ClubController {

    private final ClubService clubService;
    private final UsuarioRepository usuarioRepository;
    private final CompetidorRepository competidorRepository;

    // ✅ Constructor único para inyectar todo (Mejor práctica que @Autowired suelto)
    public ClubController(ClubService clubService,
                          UsuarioRepository usuarioRepository,
                          CompetidorRepository competidorRepository) {
        this.clubService = clubService;
        this.usuarioRepository = usuarioRepository;
        this.competidorRepository = competidorRepository;
    }

    @GetMapping
    public List<Club> listar() {
        return clubService.listar();
    }

    @GetMapping("/{id}")
    public Optional<Club> obtener(@PathVariable String id) {
        return clubService.obtener(id);
    }

    @PostMapping
    public Club crear(@RequestBody Club club) {
        return clubService.crear(club);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        clubService.eliminar(id);
    }

    // =============================================================
    // ✨ NUEVOS ENDPOINTS PARA EL DASHBOARD
    // =============================================================

    /**
     * ✅ Obtiene el club asociado al usuario logueado
     */
    @GetMapping("/mi-club")
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<Club> obtenerMiClub(Authentication auth) {
        return ResponseEntity.ok(clubService.obtenerPorUsuario(auth));
    }

    /**
     * ✅ Lógica de conteo en Backend: Solo devuelve los números (KPIs)
     */
    @GetMapping("/{idClub}/stats")
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<Map<String, Long>> getStats(@PathVariable String idClub) {
        // Esta lógica llama al método que creamos en el ClubService
        return ResponseEntity.ok(clubService.obtenerEstadisticasDashboard(idClub));
    }

    // =============================================================
    // ⚙️ GESTIÓN DE COMPETIDORES
    // =============================================================

    @PreAuthorize("hasAuthority('CLUB')") // Cambiado a Authority por consistencia
    @PutMapping("/aprobar/{idCompetidor}")
    public ResponseEntity<?> aprobar(@PathVariable String idCompetidor) {
        Competidor c = competidorRepository.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        c.setEstadoValidacion(EstadoValidacion.APROBADO);
        competidorRepository.save(c);

        Usuario u = c.getUsuario();
        u.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(u);

        return ResponseEntity.ok(Map.of("message", "Competidor aprobado y usuario activado"));
    }
}

