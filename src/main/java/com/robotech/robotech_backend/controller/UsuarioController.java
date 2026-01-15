package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.dto.UsuarioDTO;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        List<UsuarioDTO> lista = usuarioService.listarTodos().stream()
                .map(this::convertirADTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable String id) {
        Usuario u = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(convertirADTO(u));
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Map<String, String> payload) {
        try {
            String nombres = payload.get("nombres");
            String apellidos = payload.get("apellidos");
            String correo = payload.get("correo");
            String telefono = payload.get("telefono");
            String contrasena = payload.get("contrasena");
            String dni = payload.get("dni");
            String codigoClub = payload.get("codigoClub");

            CrearUsuarioDTO dto = new CrearUsuarioDTO(nombres, apellidos, correo, telefono, contrasena);
            Usuario u = usuarioService.crearUsuario(dto, dni, codigoClub);

            return ResponseEntity.ok(convertirADTO(u));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable String id, @RequestBody CrearUsuarioDTO dto) {
        Usuario u = usuarioService.actualizarUsuario(id, dto);
        return ResponseEntity.ok(convertirADTO(u));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // MÉTODO CORREGIDO
    // ==========================================
    private UsuarioDTO convertirADTO(Usuario u) {
        return new UsuarioDTO(
                u.getIdUsuario(),
                u.getNombres(),
                u.getApellidos(),
                u.getCorreo(),
                u.getRol(),
                // CORRECCIÓN: Pasamos el Enum 'EstadoUsuario' directamente.
                // Ya no usamos .name() ni Strings como "DESCONOCIDO".
                u.getEstado(),
                u.getTelefono()
        );
    }
}