package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.SolicitudIngresoDTO;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.service.SolicitudIngresoClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/club/solicitudes-ingreso")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url}")
@PreAuthorize("hasAuthority('CLUB')")
public class ClubSolicitudIngresoController {

    private final SolicitudIngresoClubService solicitudService;

    @GetMapping
    public ResponseEntity<List<SolicitudIngresoDTO>> pendientes(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(solicitudService.listarPendientesClub(usuario.getIdUsuario()));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<SolicitudIngresoDTO> aprobar(@PathVariable String id, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(solicitudService.aprobar(usuario.getIdUsuario(), id));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<SolicitudIngresoDTO> rechazar(@PathVariable String id, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(solicitudService.rechazar(usuario.getIdUsuario(), id));
    }
}


