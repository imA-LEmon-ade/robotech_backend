package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private JuezRepository juezRepository;

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
        club.getUsuario().setEstado(EstadoUsuario.ACTIVO);

        usuarioRepository.save(club.getUsuario());
        clubRepository.save(club);

        return ResponseEntity.ok("Club aprobado.");
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

        usuarioRepository.save(u);
        juezRepository.save(juez);

        return ResponseEntity.ok("Juez aprobado.");
    }
}
