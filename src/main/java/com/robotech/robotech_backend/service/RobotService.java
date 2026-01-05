package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RobotDTO;
import com.robotech.robotech_backend.model.CategoriaCompetencia;
import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Competidor;
import com.robotech.robotech_backend.model.Robot;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RobotService {

    private final RobotRepository robotRepo;
    private final CompetidorRepository competidorRepo;
    private final NicknameValidator nicknameValidator;

    public Robot crearRobot(String idCompetidor, RobotDTO dto) {

        nicknameValidator.validar(dto.getNickname());

        Competidor comp = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        // Convertir categoria (String del DTO) -> Enum
        CategoriaCompetencia categoriaEnum = parseCategoria(dto.getCategoria());

// Validar que NO tenga ya un robot en esa categoría
        if (robotRepo.existsByCompetidor_IdCompetidorAndCategoria(
                idCompetidor,
                categoriaEnum      // ✅ BIEN
        )) {
            throw new RuntimeException("Ya tienes un robot registrado en esta categoría");
        }

        if (robotRepo.existsByNickname(dto.getNickname())) {
            throw new RuntimeException("Este nickname ya está en uso");
        }
        if (robotRepo.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("Este nombre ya está en uso");
        }

        Robot robot = Robot.builder()
                .nombre(dto.getNombre())
                .categoria(categoriaEnum)   // enum en entidad
                .nickname(dto.getNickname())
                .competidor(comp)
                .build();

        return robotRepo.save(robot);
    }

    public List<Robot> listarPorCompetidor(String idCompetidor) {
        return robotRepo.findByCompetidor_IdCompetidor(idCompetidor);
    }

    public Robot editarRobot(String idRobot, RobotDTO dto) {

        Robot robot = robotRepo.findById(idRobot)
                .orElseThrow(() -> new RuntimeException("Robot no existe"));

        nicknameValidator.validar(dto.getNickname());

        CategoriaCompetencia categoriaEnum = parseCategoria(dto.getCategoria());

        // (Opcional pero recomendado) Validar duplicado si cambia la categoria
        // Si tu regla es "1 robot por categoría", esto evita que cambien a una categoría donde ya tienen robot.
        String idCompetidor = robot.getCompetidor().getIdCompetidor();
        if (robotRepo.existsByCompetidor_IdCompetidorAndCategoria(
                idCompetidor,
                categoriaEnum   // ✅ SIN .name()
        ) && robot.getCategoria() != categoriaEnum) {
            throw new RuntimeException("Ya tienes un robot registrado en esta categoría");
        }

        robot.setNombre(dto.getNombre());
        robot.setCategoria(categoriaEnum);
        robot.setNickname(dto.getNickname());

        return robotRepo.save(robot);
    }

    public void eliminar(String idRobot) {
        robotRepo.deleteById(idRobot);
    }

    public List<Robot> listarPorClub(Club club) {
        return robotRepo.findByCompetidor_Club(club);
    }

    private CategoriaCompetencia parseCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new RuntimeException("La categoría es obligatoria");
        }
        try {
            return CategoriaCompetencia.valueOf(categoria.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Categoría inválida: " + categoria);
        }
    }
}
