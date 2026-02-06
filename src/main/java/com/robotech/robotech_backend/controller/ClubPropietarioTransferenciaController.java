package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.TransferenciaPropietarioCrearDTO;
import com.robotech.robotech_backend.dto.TransferenciaPropietarioDTO;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.service.TransferenciaPropietarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/club/propietario-transferencias")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasAuthority('CLUB')")
public class ClubPropietarioTransferenciaController {

    private final TransferenciaPropietarioService transferenciaService;

    @PostMapping("/solicitar")
    public ResponseEntity<TransferenciaPropietarioDTO> solicitar(@RequestBody TransferenciaPropietarioCrearDTO dto,
                                                                 Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.solicitar(usuario.getIdUsuario(), dto));
    }

    @GetMapping
    public ResponseEntity<List<TransferenciaPropietarioDTO>> listar(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.listarPorClub(usuario.getIdUsuario()));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<TransferenciaPropietarioDTO> cancelar(@PathVariable String id, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(transferenciaService.cancelar(usuario.getIdUsuario(), id));
    }
}


