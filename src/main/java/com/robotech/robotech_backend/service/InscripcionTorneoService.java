package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscripcionTorneoService {

    private final CategoriaTorneoRepository categoriaRepo;
    private final RobotRepository robotRepo;
    private final InscripcionTorneoRepository inscripcionRepo;
    private final CompetidorRepository competidorRepo;

    // ----------------------------------------------------------------------
    // INSCRIBIR ROBOT (MODALIDAD INDIVIDUAL)
    // ----------------------------------------------------------------------
    public InscripcionTorneo inscribirIndividual(
            String idCategoriaTorneo,
            String idRobot,
            String idUsuario
    ) {

        // 1️⃣ Categoría
        CategoriaTorneo categoria = categoriaRepo.findById(idCategoriaTorneo)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 2️⃣ Validar modalidad
        if (!"INDIVIDUAL".equals(categoria.getModalidad())) {
            throw new RuntimeException("Esta categoría es por equipos");
        }

        // 3️⃣ Torneo y fechas
        Torneo torneo = categoria.getTorneo();
        Date hoy = new Date();

        if (hoy.before(torneo.getFechaAperturaInscripcion()) ||
                hoy.after(torneo.getFechaCierreInscripcion())) {
            throw new RuntimeException("Las inscripciones están fuera de fecha");
        }

        // 4️⃣ Competidor por usuario
        Competidor competidor = competidorRepo
                .findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        // 5️⃣ Robot
        Robot robot = robotRepo.findById(idRobot)
                .orElseThrow(() -> new RuntimeException("Robot no encontrado"));

        // 6️⃣ Verificar pertenencia
        if (!robot.getCompetidor().getIdCompetidor()
                .equals(competidor.getIdCompetidor())) {
            throw new RuntimeException("El robot no pertenece al competidor");
        }

        // 7️⃣ No duplicar inscripción en el mismo torneo
        boolean yaInscrito = inscripcionRepo
                .existsByRobotIdRobotAndCategoriaTorneoTorneoIdTorneo(
                        idRobot,
                        torneo.getIdTorneo()
                );

        if (yaInscrito) {
            throw new RuntimeException("El robot ya está inscrito en este torneo");
        }

        // 8️⃣ Validar cupos
        long inscritos = inscripcionRepo
                .countByCategoriaTorneoIdCategoriaTorneo(idCategoriaTorneo);

        if (inscritos >= categoria.getMaxParticipantes()) {
            throw new RuntimeException("Cupos agotados en esta categoría");
        }

        // 9️⃣ Crear inscripción
        InscripcionTorneo inscripcion = InscripcionTorneo.builder()
                .categoriaTorneo(categoria)
                .robot(robot)
                .estado("PENDIENTE")
                .build();

        return inscripcionRepo.save(inscripcion);
    }

    // ----------------------------------------------------------------------
    // APROBAR INSCRIPCIÓN
    // ----------------------------------------------------------------------
    public InscripcionTorneo aprobar(String idInscripcion) {
        InscripcionTorneo i = inscripcionRepo.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        i.setEstado("APROBADO");
        return inscripcionRepo.save(i);
    }

    // ----------------------------------------------------------------------
    // RECHAZAR INSCRIPCIÓN
    // ----------------------------------------------------------------------
    public InscripcionTorneo rechazar(String idInscripcion) {
        InscripcionTorneo i = inscripcionRepo.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        i.setEstado("RECHAZADO");
        return inscripcionRepo.save(i);
    }

    // ----------------------------------------------------------------------
    // LISTAR INSCRITOS DE UN TORNEO
    // ----------------------------------------------------------------------
    public List<?> listarInscritos(String idTorneo) {

        return inscripcionRepo
                .findByCategoriaTorneoTorneoIdTorneo(idTorneo)
                .stream()
                .map(ins -> new Object() {
                    public final String idCompetidor =
                            ins.getRobot().getCompetidor().getIdCompetidor();
                    public final String competidor =
                            ins.getRobot().getCompetidor().getUsuario().getCorreo();
                    public final String robot =
                            ins.getRobot().getNombre();
                    public final String categoria =
                            ins.getCategoriaTorneo().getCategoria();
                    public final String estado =
                            ins.getEstado();
                })
                .collect(Collectors.toList());
    }
}
