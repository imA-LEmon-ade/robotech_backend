package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.entity.Juez;
import com.robotech.robotech_backend.service.JuezService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/jueces")
@CrossOrigin(origins = "*")
public class JuezController {

    private final JuezService juezService;

    public JuezController(JuezService juezService) {
        this.juezService = juezService;
    }

    @GetMapping
    public List<Juez> listar() {
        return juezService.listar();
    }

    @GetMapping("/{id}")
    public Optional<Juez> obtener(@PathVariable String id) {
        return juezService.obtener(id);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUBADMINISTRADOR')")
    @PostMapping
    public Juez crear(@RequestBody Juez juez) {
        return juezService.crear(juez);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUBADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        juezService.eliminar(id);
    }

}


