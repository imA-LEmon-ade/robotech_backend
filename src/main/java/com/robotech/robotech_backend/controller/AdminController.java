package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "${app.frontend.url}")
public class AdminController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private JuezRepository juezRepository;
    @Autowired private CompetidorRepository competidorRepository;

    // ✔ LISTAR USUARIOS PENDIENTES
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUBADMINISTRADOR')")
    @GetMapping("/pendientes")
    public List<Usuario> listarPendientes() {
        return usuarioRepository.findByEstado(EstadoUsuario.PENDIENTE);
    }

    // ✔ APROBAR CLUB
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUBADMINISTRADOR')")
    @PutMapping("/club/{id}/aprobar")
    public ResponseEntity<?> aprobarClub(@PathVariable String id) {
        Club club = clubRepository.findById(id).orElseThrow();

        club.setEstado(EstadoClub.ACTIVO);
        Usuario usuario = club.getUsuario();
        usuario.setEstado(EstadoUsuario.ACTIVO);

        // Crear perfil de competidor para el propietario si no existe
        Competidor existente = competidorRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).orElse(null);
        if (existente == null) {
            Competidor competidor = Competidor.builder()
                    .usuario(usuario)
                    .clubActual(club)
                    .estadoValidacion(EstadoValidacion.APROBADO)
                    .build();
            competidorRepository.save(competidor);
        }

        usuario.getRoles().add(RolUsuario.CLUB);
        usuario.getRoles().add(RolUsuario.COMPETIDOR);

        usuarioRepository.save(usuario);
        clubRepository.save(club);

        return ResponseEntity.ok("Club aprobado.");
    }

    // ✔ HABILITAR PROPIETARIO COMO COMPETIDOR 
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUBADMINISTRADOR')")
    @PutMapping("/club/{id}/habilitar-competidor")
    public ResponseEntity<?> habilitarPropietarioCompetidor(@PathVariable String id) {
        Club club = clubRepository.findById(id).orElseThrow();
        Usuario usuario = club.getUsuario();

        Competidor existente = competidorRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).orElse(null);
        if (existente == null) {
            Competidor competidor = Competidor.builder()
                    .usuario(usuario)
                    .clubActual(club)
                    .estadoValidacion(EstadoValidacion.APROBADO)
                    .build();
            competidorRepository.save(competidor);
        }

        if (!usuario.getRoles().contains(RolUsuario.COMPETIDOR)) {
            usuario.getRoles().add(RolUsuario.CLUB);
            usuario.getRoles().add(RolUsuario.COMPETIDOR);
            usuarioRepository.save(usuario);
        }

        return ResponseEntity.ok("Propietario habilitado como competidor.");
    }

    // ✔ HABILITAR PROPIETARIO COMO COMPETIDOR POR ID USUARIO
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUBADMINISTRADOR')")
    @PutMapping("/club/usuario/{idUsuario}/habilitar-competidor")
    public ResponseEntity<?> habilitarPropietarioCompetidorPorUsuario(@PathVariable String idUsuario) {
        Club club = clubRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Club no encontrado para el usuario"));
        return habilitarPropietarioCompetidor(club.getIdClub());
    }

    // ✔ APROBAR JUEZ
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUBADMINISTRADOR')")
    @PutMapping("/juez/{id}/aprobar")
    public ResponseEntity<?> aprobarJuez(@PathVariable String id) {
        Juez juez = juezRepository.findById(id).orElseThrow();

        juez.setEstadoValidacion(EstadoValidacion.APROBADO);
        juez.setValidadoEn(new Date());

        Usuario u = juez.getUsuario();
        u.setEstado(EstadoUsuario.ACTIVO);
        u.getRoles().add(RolUsuario.JUEZ);

        Competidor comp = competidorRepository.findByUsuario_IdUsuario(u.getIdUsuario()).orElse(null);
        if (comp != null) {
            comp.setClubActual(null); // Agente libre
            competidorRepository.save(comp);
            u.getRoles().add(RolUsuario.COMPETIDOR);
        }

        usuarioRepository.save(u);
        juezRepository.save(juez);

        return ResponseEntity.ok("Juez aprobado.");
    }
}


