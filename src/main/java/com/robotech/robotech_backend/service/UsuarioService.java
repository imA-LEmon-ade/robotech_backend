package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    public Usuario crearUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public boolean correoExiste(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }


    public boolean telefonoExiste(String telefono) {
        return usuarioRepository.existsByTelefono(telefono);
    }

    public Optional<Usuario> login(String correo, String contrasena) {
        return usuarioRepository.findByCorreoAndContrasenaHash(correo, contrasena);
    }

    public Optional<Usuario> actualizarUsuario(String id, Usuario datos) {
        return usuarioRepository.findById(id).map(u -> {
            u.setCorreo(datos.getCorreo());
            u.setTelefono(datos.getTelefono());
            u.setContrasenaHash(datos.getContrasenaHash());
            u.setRol(datos.getRol());
            u.setEstado(datos.getEstado());
            return usuarioRepository.save(u);
        });
    }

    public boolean eliminarUsuario(String id) {
        if (!usuarioRepository.existsById(id)) {
            return false;
        }
        usuarioRepository.deleteById(id);
        return true;
    }


}
