package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.RobotAdminDTO;
import com.robotech.robotech_backend.dto.PageResponse;
import com.robotech.robotech_backend.service.AdminRobotService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

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
    public PageResponse<RobotAdminDTO> listarRobots(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String idClub
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by("idRobot").ascending()
        );

        Page<RobotAdminDTO> result = adminRobotService.listarRobots(nombre, categoria, idClub, pageable);

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}

