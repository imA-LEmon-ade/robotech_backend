package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.RobotDTO;
import com.robotech.robotech_backend.service.RobotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/competidor/robots")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CompetidorRobotController {

    private final RobotService robotService;

    @PostMapping("/{idCompetidor}")
    public ResponseEntity<?> crear(@PathVariable String idCompetidor, @RequestBody RobotDTO dto) {
        return ResponseEntity.ok(robotService.crearRobot(idCompetidor, dto));
    }

    @GetMapping("/{idCompetidor}")
    public ResponseEntity<?> listar(@PathVariable String idCompetidor) {
        return ResponseEntity.ok(robotService.listarPorCompetidor(idCompetidor));
    }

    @PutMapping("/{idRobot}")
    public ResponseEntity<?> editar(@PathVariable String idRobot, @RequestBody RobotDTO dto) {
        return ResponseEntity.ok(robotService.editarRobot(idRobot, dto));
    }

    @DeleteMapping("/{idRobot}")
    public ResponseEntity<?> eliminar(@PathVariable String idRobot) {
        robotService.eliminar(idRobot);
        return ResponseEntity.ok("Robot eliminado");
    }
}
