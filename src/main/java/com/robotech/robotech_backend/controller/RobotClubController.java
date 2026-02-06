package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.RobotDTO; // 👈 FALTABA ESTA IMPORTACIÓN
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.service.ClubService;
import com.robotech.robotech_backend.service.RobotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/club/robots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLUB')")
@CrossOrigin(origins = "${app.frontend.url}")
public class RobotClubController {

    private final RobotService robotService;
    private final ClubService clubService;

    @GetMapping
    public List<RobotDTO> misRobots(Authentication auth) {
        // Obtenemos el club del usuario logueado
        Club club = clubService.obtenerPorUsuario(auth);

        // Llamamos al servicio que ahora devuelve DTOs limpios (sin proxies)
        return robotService.listarPorClub(club);
    }
}

