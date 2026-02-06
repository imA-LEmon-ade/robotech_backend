package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.TransferenciaPropietarioDTO;
import com.robotech.robotech_backend.service.TransferenciaPropietarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/propietario-transferencias")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUBADMINISTRADOR')")
public class AdminPropietarioTransferenciaController {

    private final TransferenciaPropietarioService transferenciaService;

    @GetMapping("/pendientes")
    public ResponseEntity<List<TransferenciaPropietarioDTO>> listarPendientes() {
        return ResponseEntity.ok(transferenciaService.listarPendientes());
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<TransferenciaPropietarioDTO> aprobar(@PathVariable String id) {
        return ResponseEntity.ok(transferenciaService.aprobar(id));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<TransferenciaPropietarioDTO> rechazar(@PathVariable String id) {
        return ResponseEntity.ok(transferenciaService.rechazar(id));
    }
}


