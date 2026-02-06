package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RobotDTO;
import com.robotech.robotech_backend.dto.RobotResponseDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RobotService {

    private final RobotRepository robotRepo;
    private final CompetidorRepository competidorRepo;
    private final com.robotech.robotech_backend.service.validadores.NicknameValidator nicknameValidator;

    /**
     * Crea un nuevo robot validando reglas de negocio.
     */
    @Transactional
    public RobotResponseDTO crearRobot(String idCompetidor, RobotDTO dto) {
        System.out.println("🔵 [RobotService] INICIO CREAR ROBOT");
        System.out.println("   -> Competidor ID: " + idCompetidor);
        System.out.println("   -> Datos recibidos: Nombre='" + dto.getNombre() + "', Nick='" + dto.getNickname() + "', Cat='" + dto.getCategoria() + "'");

        // 1. Limpieza de datos
        String nombreLimpio = dto.getNombre().trim();
        String nicknameLimpio = dto.getNickname().trim();
        String categoriaLimpia = dto.getCategoria().trim();

        System.out.println("   -> Datos limpios: '" + nombreLimpio + "', '" + nicknameLimpio + "', '" + categoriaLimpia + "'");

        // 2. Validaciones de texto
        try {
            if (nicknameValidator == null) {
                System.err.println("❌ ERROR CRÍTICO: NicknameValidator no fue inyectado (es null).");
                throw new RuntimeException("Error interno: Validator nulo.");
            }
            System.out.println("   -> Validando contenido de texto...");
            nicknameValidator.validar(nicknameLimpio);
            nicknameValidator.validar(nombreLimpio);
        } catch (RuntimeException e) {
            System.err.println("❌ Error de validación de texto: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        // 3. Buscar competidor
        System.out.println("   -> Buscando competidor en BD...");
        Competidor comp = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> {
                    System.err.println("❌ Competidor no encontrado.");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Competidor no encontrado");
                });
        System.out.println("   -> Competidor encontrado: " + comp.getUsuario().getDni());

        // 4. Parsear categoría
        CategoriaCompetencia categoriaEnum = parseCategoria(categoriaLimpia);
        System.out.println("   -> Categoría parseada correctamente: " + categoriaEnum);

        // 🔒 RESTRICCIÓN 1: Un solo robot por categoría
        System.out.println("   -> Verificando duplicados de categoría...");
        if (robotRepo.existsByCompetidor_IdCompetidorAndCategoria(idCompetidor, categoriaEnum)) {
            System.err.println("❌ RECHAZADO: Ya tiene robot en " + categoriaEnum);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya tienes un robot registrado en la categoría " + categoriaEnum + ". Debes eliminar el anterior o editarlo.");
        }

        // 🔒 RESTRICCIÓN 2: Nickname único Global
        System.out.println("   -> Verificando unicidad de Nickname...");
        if (robotRepo.existsByNickname(nicknameLimpio)) {
            System.err.println("❌ RECHAZADO: Nickname '" + nicknameLimpio + "' ya existe.");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nickname '" + nicknameLimpio + "' ya está en uso por otro competidor.");
        }

        // 🔒 RESTRICCIÓN 3: Nombre único Global
        System.out.println("   -> Verificando unicidad de Nombre...");
        if (robotRepo.existsByNombre(nombreLimpio)) {
            System.err.println("❌ RECHAZADO: Nombre '" + nombreLimpio + "' ya existe.");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nombre '" + nombreLimpio + "' ya existe.");
        }

        // 5. Construir
        System.out.println("   -> Construyendo entidad Robot...");
        Robot robot = Robot.builder()
                //.idRobot(UUID.randomUUID().toString()) // 🔥 ASEGÚRATE DE QUE ESTO ESTÉ DESCOMENTADO SI TU BD NO ES AUTO_INCREMENT
                .nombre(nombreLimpio)
                .categoria(categoriaEnum)
                .nickname(nicknameLimpio)
                .estado(EstadoRobot.ACTIVO)
                .competidor(comp)
                .build();

        // 6. Guardar
        System.out.println("   -> Guardando en repositorio...");
        Robot saved = robotRepo.save(robot);
        System.out.println("✅ [RobotService] ÉXITO. Robot guardado con ID: " + saved.getIdRobot());

        return mapToResponse(saved);
    }

    /**
     * Lista los robots de un competidor.
     */
    public List<RobotDTO> listarPorCompetidor(String idCompetidor) {
        return robotRepo.findByCompetidor_IdCompetidor(idCompetidor)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Edita un robot existente.
     */
    @Transactional
    public RobotResponseDTO editarRobot(String idRobot, RobotDTO dto) {
        System.out.println("🟠 [RobotService] INICIO EDITAR ROBOT: " + idRobot);

        Robot robot = robotRepo.findById(idRobot)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El robot no existe"));

        // 1. Limpieza
        String nombreLimpio = dto.getNombre().trim();
        String nicknameLimpio = dto.getNickname().trim();

        // 2. Validaciones texto
        try {
            nicknameValidator.validar(nicknameLimpio);
            nicknameValidator.validar(nombreLimpio);
        } catch (RuntimeException e) {
            System.err.println("❌ Error validación texto al editar: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        CategoriaCompetencia nuevaCategoria = parseCategoria(dto.getCategoria());

        // 🔒 RESTRICCIÓN 1: Cambio de categoría
        if (robot.getCategoria() != nuevaCategoria) {
            System.out.println("   -> Intento de cambio de categoría de " + robot.getCategoria() + " a " + nuevaCategoria);
            String idCompetidor = robot.getCompetidor().getIdCompetidor();
            if (robotRepo.existsByCompetidor_IdCompetidorAndCategoria(idCompetidor, nuevaCategoria)) {
                System.err.println("❌ RECHAZADO: Ya tiene otro robot en la nueva categoría.");
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No puedes cambiar a " + nuevaCategoria + " porque ya tienes otro robot en esa categoría.");
            }
        }

        // 🔒 RESTRICCIÓN 2: Nickname único
        if (!robot.getNickname().equalsIgnoreCase(nicknameLimpio)) {
            System.out.println("   -> Cambio de Nickname detectado. Verificando disponibilidad...");
            if (robotRepo.existsByNickname(nicknameLimpio)) {
                System.err.println("❌ RECHAZADO: Nickname ocupado.");
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El nickname '" + nicknameLimpio + "' ya está en uso.");
            }
        }

        // 🔒 RESTRICCIÓN 3: Nombre único
        if (!robot.getNombre().equalsIgnoreCase(nombreLimpio)) {
            System.out.println("   -> Cambio de Nombre detectado. Verificando disponibilidad...");
            if (robotRepo.existsByNombre(nombreLimpio)) {
                System.err.println("❌ RECHAZADO: Nombre ocupado.");
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El nombre '" + nombreLimpio + "' ya está en uso.");
            }
        }

        robot.setNombre(nombreLimpio);
        robot.setCategoria(nuevaCategoria);
        robot.setNickname(nicknameLimpio);

        Robot saved = robotRepo.save(robot);
        System.out.println("✅ [RobotService] EDICIÓN EXITOSA.");
        return mapToResponse(saved);
    }

    public void eliminar(String idRobot) {
        if (!robotRepo.existsById(idRobot)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El robot no existe");
        }
        robotRepo.deleteById(idRobot);
    }

    public List<RobotDTO> listarPorClub(Club club) {
        return robotRepo.findByCompetidor_ClubActual(club)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- Métodos Privados Auxiliares ---

    private RobotDTO mapToDTO(Robot robot) {
        return new RobotDTO(
                robot.getNombre(),
                robot.getCategoria().name(),
                robot.getNickname(),
                robot.getIdRobot()
        );
    }

    private RobotResponseDTO mapToResponse(Robot robot) {
        return new RobotResponseDTO(
                robot.getIdRobot(),
                robot.getNombre(),
                robot.getNickname(),
                robot.getCategoria().name(),
                robot.getEstado().name()
        );
    }

    private CategoriaCompetencia parseCategoria(String categoria) {
        System.out.println("   -> Parseando categoría: '" + categoria + "'");
        if (categoria == null || categoria.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría es obligatoria");
        }
        try {
            return CategoriaCompetencia.valueOf(categoria.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Error al convertir categoría: " + categoria);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Categoría inválida: " + categoria + ". Verifique los valores permitidos.");
        }
    }
}

