package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.RobotAdminDTO;
import com.robotech.robotech_backend.service.AdminRobotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/robots")
@RequiredArgsConstructor
public class AdminRobotController {

    private final AdminRobotService adminRobotService;

    // -------------------------------------------------
    // LISTAR / FILTRAR ROBOTS (ADMIN)
    // -------------------------------------------------
    // Filtros opcionales:
    // - nombre
    // - categoria (MINISUMO, MICROSUMO, etc)
    // - idClub
    // -------------------------------------------------
    @GetMapping
    public List<RobotAdminDTO> listarRobots(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String idClub
    ) {
        return adminRobotService.listarRobots(nombre, categoria, idClub);
    }
}