package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.JuezEstadoDTO;
import com.robotech.robotech_backend.dto.JuezPostulacionDTO;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.service.PostulacionJuezService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/competidor/juez")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasAuthority('COMPETIDOR')")
public class CompetidorJuezController {

    private final PostulacionJuezService postulacionService;

    @GetMapping("/estado")
    public ResponseEntity<JuezEstadoDTO> estado(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(postulacionService.obtenerEstado(usuario.getIdUsuario()));
    }

    @PostMapping("/postular")
    public ResponseEntity<JuezEstadoDTO> postular(@RequestBody JuezPostulacionDTO dto, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(postulacionService.postular(usuario.getIdUsuario(), dto));
    }
}


