package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RobotDTO;
import com.robotech.robotech_backend.dto.RobotResponseDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
// Asegúrate de que este import apunte a donde tengas tu validador (ej: .utils o .service)
import com.robotech.robotech_backend.service.NicknameValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RobotService {

    private final RobotRepository robotRepo;
    private final CompetidorRepository competidorRepo;
    private final NicknameValidator nicknameValidator;

    /**
     * Crea un nuevo robot validando reglas de negocio.
     */
    @Transactional
    public RobotResponseDTO crearRobot(String idCompetidor, RobotDTO dto) {
        // Validaciones
        nicknameValidator.validar(dto.getNickname());
        nicknameValidator.validar(dto.getNombre());

        Competidor comp = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        CategoriaCompetencia categoriaEnum = parseCategoria(dto.getCategoria());

        if (robotRepo.existsByCompetidor_IdCompetidorAndCategoria(idCompetidor, categoriaEnum)) {
            throw new RuntimeException("Ya tienes un robot registrado en la categoría " + categoriaEnum);
        }
        if (robotRepo.existsByNickname(dto.getNickname())) {
            throw new RuntimeException("El nickname '" + dto.getNickname() + "' ya está en uso.");
        }
        if (robotRepo.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("El nombre '" + dto.getNombre() + "' ya está en uso.");
        }

        Robot robot = Robot.builder()
                .nombre(dto.getNombre())
                .categoria(categoriaEnum)
                .nickname(dto.getNickname())
                .estado(EstadoRobot.ACTIVO)
                .competidor(comp)
                .build();

        Robot saved = robotRepo.save(robot);

        return new RobotResponseDTO(
                saved.getIdRobot(),
                saved.getNombre(),
                saved.getNickname(),
                saved.getCategoria().name(),
                saved.getEstado().name()
        );
    }

    /**
     * Lista los robots de un competidor.
     */
    public List<RobotDTO> listarPorCompetidor(String idCompetidor) {
        return robotRepo.findByCompetidor_IdCompetidor(idCompetidor)
                .stream()
                .map(this::mapToDTO) // Refactorizado para usar método auxiliar
                .toList();
    }

    /**
     * Edita un robot existente.
     */
    @Transactional
    public RobotResponseDTO editarRobot(String idRobot, RobotDTO dto) {
        Robot robot = robotRepo.findById(idRobot)
                .orElseThrow(() -> new RuntimeException("El robot no existe"));

        nicknameValidator.validar(dto.getNickname());
        nicknameValidator.validar(dto.getNombre());

        CategoriaCompetencia nuevaCategoria = parseCategoria(dto.getCategoria());

        if (robot.getCategoria() != nuevaCategoria) {
            String idCompetidor = robot.getCompetidor().getIdCompetidor();
            if (robotRepo.existsByCompetidor_IdCompetidorAndCategoria(idCompetidor, nuevaCategoria)) {
                throw new RuntimeException("Ya tienes un robot en la categoría " + nuevaCategoria);
            }
        }

        if (!robot.getNickname().equalsIgnoreCase(dto.getNickname()) && robotRepo.existsByNickname(dto.getNickname())) {
            throw new RuntimeException("El nickname '" + dto.getNickname() + "' ya está en uso.");
        }

        if (!robot.getNombre().equalsIgnoreCase(dto.getNombre()) && robotRepo.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("El nombre '" + dto.getNombre() + "' ya está en uso.");
        }

        robot.setNombre(dto.getNombre());
        robot.setCategoria(nuevaCategoria);
        robot.setNickname(dto.getNickname());

        Robot saved = robotRepo.save(robot);

        return new RobotResponseDTO(
                saved.getIdRobot(),
                saved.getNombre(),
                saved.getNickname(),
                saved.getCategoria().name(),
                saved.getEstado().name()
        );
    }

    public void eliminar(String idRobot) {
        if (!robotRepo.existsById(idRobot)) {
            throw new RuntimeException("El robot no existe");
        }
        robotRepo.deleteById(idRobot);
    }

    /**
     * ⚠️ CORREGIDO: Ahora devuelve List<RobotDTO>.
     * Esto soluciona el error "ByteBuddyInterceptor" al listar robots en el panel del Club.
     */
    public List<RobotDTO> listarPorClub(Club club) {
        return robotRepo.findByCompetidor_ClubActual(club)
                .stream()
                .map(this::mapToDTO) // Convertimos a DTO para romper la relación lazy
                .toList();
    }

    // --- Métodos Privados Auxiliares ---

    // Método para convertir Robot -> RobotDTO y no repetir código
    private RobotDTO mapToDTO(Robot robot) {
        return new RobotDTO(
                robot.getNombre(),
                robot.getCategoria().name(),
                robot.getNickname(),
                robot.getIdRobot()
        );
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