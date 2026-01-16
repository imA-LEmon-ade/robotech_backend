package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.EncuentroDetalleJuezDTO;
import com.robotech.robotech_backend.dto.EncuentroJuezDTO;
import com.robotech.robotech_backend.dto.RegistrarResultadoEncuentroDTO;
import com.robotech.robotech_backend.model.Encuentro;
import com.robotech.robotech_backend.model.Juez;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.JuezRepository;
import com.robotech.robotech_backend.service.EncuentroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/juez/encuentros")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasAuthority('JUEZ')")
public class JuezEncuentrosController {

    private final EncuentroService encuentroService;
    private final JuezRepository juezRepo;


    // 1️⃣ Listar encuentros asignados
    @GetMapping
    public ResponseEntity<List<EncuentroJuezDTO>> misEncuentros(Authentication auth) {

        Usuario usuario = (Usuario) auth.getPrincipal(); // 🔥 AQUÍ ESTÁ LA CLAVE

        String idUsuario = usuario.getIdUsuario();

        Juez juez = juezRepo.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        return ResponseEntity.ok(
                encuentroService.listarEncuentrosPorJuez(juez.getIdJuez())
        );
    }


    @GetMapping("/{idEncuentro}")
    public ResponseEntity<EncuentroDetalleJuezDTO> obtenerDetalle(
            @PathVariable String idEncuentro,
            Authentication auth
    ) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(
                encuentroService.obtenerDetalleParaJuez(usuario.getIdUsuario(), idEncuentro)
        );
    }

    @PostMapping("/{idEncuentro}/resultado")
    public ResponseEntity<Encuentro> registrarResultado(
            @PathVariable String idEncuentro,
            @RequestBody RegistrarResultadoEncuentroDTO dto,
            Authentication auth
    ) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Juez juez = juezRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        dto.setIdEncuentro(idEncuentro);

        return ResponseEntity.ok(
                encuentroService.registrarResultado(juez.getIdJuez(), dto)
        );
    }
}