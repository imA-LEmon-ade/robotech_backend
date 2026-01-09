package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/clubes")
@CrossOrigin(origins = "*")
public class ClubController {

    private final ClubService clubService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CompetidorRepository competidorRepository;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }


    @GetMapping
    public List<Club> listar() {
        return clubService.listar();
    }

    @GetMapping("/{id}")
    public Optional<Club> obtener(@PathVariable String id) {
        return clubService.obtener(id);
    }

    //@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUBADMINISTRADOR')")
    @PostMapping
    public Club crear(@RequestBody Club club) {
        return clubService.crear(club);
    }

    //@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUBADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        clubService.eliminar(id);
    }

    @PreAuthorize("hasRole('CLUB')")
    @PutMapping("/aprobar/{idCompetidor}")
    public ResponseEntity<?> aprobar(@PathVariable String idCompetidor) {
        Competidor c = competidorRepository.findById(idCompetidor).orElseThrow();

        c.setEstadoValidacion(EstadoValidacion.APROBADO);
        competidorRepository.save(c);

        Usuario u = c.getUsuario();
        u.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(u);

        return ResponseEntity.ok("Competidor aprobado");
    }




}
