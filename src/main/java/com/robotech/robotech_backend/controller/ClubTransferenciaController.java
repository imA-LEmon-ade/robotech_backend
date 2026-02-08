package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.TransferenciaCrearDTO;
import com.robotech.robotech_backend.dto.TransferenciaDTO;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.service.TransferenciaCompetidorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/club/transferencias")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url}")
@PreAuthorize("hasAuthority('CLUB')")
public class ClubTransferenciaController {

    private final TransferenciaCompetidorService transferenciaService;

    @PostMapping("/publicar")
    public ResponseEntity<TransferenciaDTO> publicar(@RequestBody TransferenciaCrearDTO dto, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.publicar(usuario.getIdUsuario(), dto));
    }

    @GetMapping("/mercado")
    public ResponseEntity<List<TransferenciaDTO>> mercado(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.listarMercado(usuario.getIdUsuario()));
    }

    @PostMapping("/{id}/solicitar")
    public ResponseEntity<TransferenciaDTO> solicitar(@PathVariable String id, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.solicitar(usuario.getIdUsuario(), id));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<TransferenciaDTO> aprobar(@PathVariable String id, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.aprobar(usuario.getIdUsuario(), id));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<TransferenciaDTO> rechazar(@PathVariable String id, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.rechazar(usuario.getIdUsuario(), id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<TransferenciaDTO> cancelar(@PathVariable String id, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.cancelar(usuario.getIdUsuario(), id));
    }

    @GetMapping("/mis-publicaciones")
    public ResponseEntity<List<TransferenciaDTO>> misPublicaciones(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.misPublicaciones(usuario.getIdUsuario()));
    }

    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<TransferenciaDTO>> misSolicitudes(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.misSolicitudes(usuario.getIdUsuario()));
    }
}


