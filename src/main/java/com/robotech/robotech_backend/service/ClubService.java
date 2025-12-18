package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClubService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final ClubRepository clubRepository;

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public List<Club> listar() { return clubRepository.findAll(); }

    public Optional<Club> obtener(String id) { return clubRepository.findById(id); }

    public Club crear(Club club) {

        // Validar que el usuario existe
        Usuario usuarioReal = usuarioRepository.findById(club.getUsuario().getIdUsuario())
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));

        // Asignar el usuario real al club
        club.setUsuario(usuarioReal);

        // Guardar club con usuario real
        return clubRepository.save(club);
    }

    public void eliminar(String id) { clubRepository.deleteById(id); }


    public Club obtenerPorUsuario(Authentication auth) {

        String correo = auth.getName(); // viene del JWT

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return clubRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Club no asociado al usuario"));
    }
}
