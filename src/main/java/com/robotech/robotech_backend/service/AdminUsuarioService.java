package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.UsuarioDTO;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepo;

    // LISTAR
    public List<Usuario> listar() {
        return usuarioRepo.findAll();
    }

    // CREAR
    public Usuario crear(UsuarioDTO dto) {

        Usuario u = Usuario.builder()
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(dto.getContrasena())
                .rol(dto.getRol())
                .estado(dto.getEstado())
                .build();

        return usuarioRepo.save(u);
    }

    // EDITAR
    public Usuario editar(String id, UsuarioDTO dto) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setCorreo(dto.getCorreo());
        u.setTelefono(dto.getTelefono());
        u.setRol(dto.getRol());
        u.setEstado(dto.getEstado());

        return usuarioRepo.save(u);
    }

    // CAMBIAR ESTADO
    public Usuario cambiarEstado(String id, String nuevoEstado) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setEstado(nuevoEstado);
        return usuarioRepo.save(u);
    }

    // CAMBIAR CONTRASEÑA
    public Usuario cambiarPassword(String id, String nuevaPass) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setContrasenaHash(nuevaPass);
        return usuarioRepo.save(u);
    }

    // ELIMINAR
    public void eliminar(String id) {
        usuarioRepo.deleteById(id);
    }
}
