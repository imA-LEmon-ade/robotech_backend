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

    // 1️⃣ Listar encuentros asignados al juez logueado
    @GetMapping
    public ResponseEntity<List<EncuentroJuezDTO>> misEncuentros(Authentication auth) {
        // Obtenemos el usuario autenticado desde el SecurityContext
        Usuario usuario = (Usuario) auth.getPrincipal();

        Juez juez = juezRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Perfil de Juez no encontrado para este usuario"));

        return ResponseEntity.ok(
                encuentroService.listarEncuentrosPorJuez(juez.getIdJuez())
        );
    }

    // 2️⃣ Obtener el detalle de un encuentro específico para el panel de calificación
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

    // 3️⃣ Registrar el resultado, guardar puntajes y finalizar torneo si es el último
    @PostMapping("/{idEncuentro}/resultado")
    public ResponseEntity<Encuentro> registrarResultado(
            @PathVariable String idEncuentro,
            @RequestBody RegistrarResultadoEncuentroDTO dto,
            Authentication auth
    ) {
        // Recuperamos al Juez
        Usuario usuario = (Usuario) auth.getPrincipal();
        Juez juez = juezRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Perfil de Juez no encontrado"));

        // Sincronizamos el ID de la URL con el DTO
        dto.setIdEncuentro(idEncuentro);

        // Llamamos al servicio (que ya contiene la lógica de cierre automático del torneo)
        Encuentro resultado = encuentroService.registrarResultado(juez.getIdJuez(), dto);

        return ResponseEntity.ok(resultado);
    }
}