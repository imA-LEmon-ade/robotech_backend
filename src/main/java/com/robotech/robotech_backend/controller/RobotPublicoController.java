package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.PageResponse;
import com.robotech.robotech_backend.dto.RobotPublicoDTO;
import com.robotech.robotech_backend.service.RobotPublicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/robots")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url}")
public class RobotPublicoController {

    private final RobotPublicoService robotPublicoService;

    @GetMapping
    public ResponseEntity<PageResponse<RobotPublicoDTO>> listarRobotsPublicos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String q
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 60);
        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by("nombre").ascending()
        );
        return ResponseEntity.ok(robotPublicoService.obtenerRobotsPublicos(pageable, q));
    }
}
