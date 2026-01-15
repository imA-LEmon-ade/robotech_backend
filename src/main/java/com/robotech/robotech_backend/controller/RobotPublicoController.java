package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.RobotPublicoDTO;
import com.robotech.robotech_backend.service.RobotPublicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/robots") // 👈 Esta es la ruta que te faltaba
@RequiredArgsConstructor
@CrossOrigin("*") // Permite que React se conecte
public class RobotPublicoController {

    private final RobotPublicoService robotPublicoService;

    @GetMapping
    public ResponseEntity<List<RobotPublicoDTO>> listarRobotsPublicos() {
        return ResponseEntity.ok(robotPublicoService.obtenerRobotsPublicos());
    }
}