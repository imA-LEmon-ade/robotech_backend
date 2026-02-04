package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.SolicitudIngresoCrearDTO;
import com.robotech.robotech_backend.dto.SolicitudIngresoDTO;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.service.SolicitudIngresoClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competidor/club-solicitudes")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasAuthority('COMPETIDOR')")
public class CompetidorClubSolicitudController {

    private final SolicitudIngresoClubService solicitudService;

    @PostMapping
    public ResponseEntity<SolicitudIngresoDTO> solicitar(@RequestBody SolicitudIngresoCrearDTO dto, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(solicitudService.solicitar(usuario.getIdUsuario(), dto));
    }

    @GetMapping
    public ResponseEntity<List<SolicitudIngresoDTO>> misSolicitudes(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(solicitudService.listarMisSolicitudes(usuario.getIdUsuario()));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<SolicitudIngresoDTO> cancelar(@PathVariable String id, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(solicitudService.cancelar(usuario.getIdUsuario(), id));
    }
}
