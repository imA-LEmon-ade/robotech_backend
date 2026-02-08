package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.RobotDTO;
import com.robotech.robotech_backend.service.RobotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competidor/robots")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url}") // Permite peticiones desde React
public class CompetidorRobotController {

    private final RobotService robotService;
    // ❌ Se eliminó RobotRepository (innecesario y mala práctica aquí)

    // 1. Crear Robot
    @PostMapping("/{idCompetidor}")
    public ResponseEntity<?> crear(
            @PathVariable String idCompetidor,
            @RequestBody RobotDTO dto
    ) {
        // Llama al servicio que ya valida groserías y reglas de negocio
        return ResponseEntity.ok(robotService.crearRobot(idCompetidor, dto));
    }

    // 2. Listar Robots por Competidor
    @GetMapping("/{idCompetidor}")
    public ResponseEntity<List<RobotDTO>> listar(@PathVariable String idCompetidor) {
        return ResponseEntity.ok(robotService.listarPorCompetidor(idCompetidor));
    }

    // 3. Editar Robot
    @PutMapping("/{idRobot}")
    public ResponseEntity<?> editar(
            @PathVariable String idRobot,
            @RequestBody RobotDTO dto
    ) {
        return ResponseEntity.ok(robotService.editarRobot(idRobot, dto));
    }

    // 4. Eliminar Robot
    @DeleteMapping("/{idRobot}")
    public ResponseEntity<?> eliminar(@PathVariable String idRobot) {
        robotService.eliminar(idRobot);
        return ResponseEntity.ok("Robot eliminado correctamente");
    }
}

