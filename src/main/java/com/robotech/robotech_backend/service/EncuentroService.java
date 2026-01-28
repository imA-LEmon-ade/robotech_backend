package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.*;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EncuentroService {

    private final CategoriaTorneoRepository categoriaRepo;
    private final InscripcionTorneoRepository inscripcionRepo;
    private final EquipoTorneoRepository equipoRepo;
    private final EncuentroRepository encuentroRepo;
    private final JuezRepository juezRepo;
    private final ColiseoRepository coliseoRepo;
    private final EncuentroParticipanteRepository encuentroParticipanteRepo;
    private final HistorialCalificacionRepository historialRepo;
    private final RobotRepository robotRepo;
    private final TorneoRepository torneoRepo;

    // =====================================================
    // 1. GENERAR ENCUENTROS
    // =====================================================
    public List<EncuentroAdminDTO> generarEncuentros(CrearEncuentrosDTO dto) {
        try {
            List<Encuentro> encuentros = generarEncuentrosInterno(dto);
            return encuentros.stream()
                    .map(this::toAdminDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ ERROR AL GENERAR ENCUENTROS: " + e.getMessage());
            throw e;
        }
    }

    // =====================================================
    // 2. REGISTRAR RESULTADO
    // =====================================================
    @Transactional
    public Encuentro registrarResultado(String idJuez, RegistrarResultadoEncuentroDTO dto) {
        Encuentro encuentro = encuentroRepo.findById(dto.getIdEncuentro())
                .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));

        if (!encuentro.getJuez().getIdJuez().equals(idJuez)) {
            throw new RuntimeException("No tiene autorización para calificar este encuentro.");
        }

        if (encuentro.getEstado() == EstadoEncuentro.FINALIZADO) {
            throw new RuntimeException("El encuentro ya fue finalizado previamente.");
        }

        List<EncuentroParticipante> participantes =
                encuentroParticipanteRepo.findByEncuentroIdEncuentro(encuentro.getIdEncuentro());

        Map<String, Integer> puntajes = dto.getCalificaciones().stream()
                .collect(Collectors.toMap(
                        CalificacionParticipanteDTO::getIdReferencia,
                        CalificacionParticipanteDTO::getCalificacion
                ));

        for (EncuentroParticipante participante : participantes) {
            Integer calificacion = puntajes.get(participante.getIdReferencia());
            if (calificacion == null) throw new RuntimeException("Falta puntaje para: " + participante.getIdReferencia());
            participante.setCalificacion(calificacion);
        }

        int maxPuntaje = participantes.stream().mapToInt(EncuentroParticipante::getCalificacion).max().orElseThrow();
        List<EncuentroParticipante> ganadores = participantes.stream().filter(p -> p.getCalificacion() == maxPuntaje).toList();

        if (ganadores.size() > 1) {
            throw new RuntimeException("Empate detectado. No se puede definir un ganador.");
        }

        EncuentroParticipante ganador = ganadores.get(0);

        for (EncuentroParticipante participante : participantes) {
            boolean esGanador = participante.getIdReferencia().equals(ganador.getIdReferencia());
            participante.setGanador(esGanador);

            historialRepo.save(HistorialCalificacion.builder()
                    .encuentro(encuentro)
                    .tipo(participante.getTipo())
                    .idReferencia(participante.getIdReferencia())
                    .puntaje(participante.getCalificacion())
                    .build());
        }

        encuentro.setGanadorIdReferencia(ganador.getIdReferencia());
        encuentro.setGanadorTipo(ganador.getTipo());
        encuentro.setEstado(EstadoEncuentro.FINALIZADO);

        encuentroParticipanteRepo.saveAll(participantes);
        Encuentro guardado = encuentroRepo.save(encuentro);

        // Cierre automático del torneo
        try {
            String idTorneo = encuentro.getCategoriaTorneo().getTorneo().getIdTorneo();
            long pendientes = encuentroRepo.countByCategoriaTorneo_Torneo_IdTorneoAndEstadoNot(idTorneo, EstadoEncuentro.FINALIZADO);
            if (pendientes == 0) {
                Torneo torneo = encuentro.getCategoriaTorneo().getTorneo();
                torneo.setEstado("FINALIZADO");
                torneoRepo.save(torneo);
            }
        } catch (Exception ignored) {}

        return guardado;
    }

    // =====================================================
    // 3. VISTAS PARA EL JUEZ (CORREGIDO PARA MOSTRAR NOMBRES)
    // =====================================================
    public List<EncuentroJuezDTO> listarEncuentrosPorJuez(String idJuez) {
        return encuentroRepo.findByJuezIdJuez(idJuez).stream().map(encuentro -> {

            // Mapeo de participantes incluyendo el nombre real
            List<ParticipanteEncuentroDTO> participantesDTO = encuentroParticipanteRepo
                    .findByEncuentroIdEncuentro(encuentro.getIdEncuentro()).stream()
                    .map(p -> ParticipanteEncuentroDTO.builder()
                            .idReferencia(p.getIdReferencia())
                            .nombre(obtenerNombreParticipante(p.getTipo(), p.getIdReferencia()))
                            .tipo(p.getTipo())
                            .calificacion(p.getCalificacion())
                            .ganador(p.getGanador())
                            .build())
                    .toList();

            // Retorno del DTO con el nombre del torneo cargado
            return EncuentroJuezDTO.builder()
                    .idEncuentro(encuentro.getIdEncuentro())
                    .nombreTorneo(encuentro.getCategoriaTorneo().getTorneo().getNombre())
                    .categoria(encuentro.getCategoriaTorneo().getCategoria().name())
                    .tipo(encuentro.getTipo())
                    .estado(encuentro.getEstado())
                    .coliseo(encuentro.getColiseo().getNombre())
                    .fecha(encuentro.getFecha())
                    .participantes(participantesDTO)
                    .build();
        }).toList();
    }

    public EncuentroDetalleJuezDTO obtenerDetalleParaJuez(String idUsuario, String idEncuentro) {
        Juez juez = juezRepo.findByUsuario_IdUsuario(idUsuario).orElseThrow(() -> new RuntimeException("Juez no encontrado"));
        Encuentro encuentro = encuentroRepo.findById(idEncuentro).orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));
        if (!encuentro.getJuez().getIdJuez().equals(juez.getIdJuez())) throw new RuntimeException("No autorizado");

        List<ParticipanteJuezDTO> participantesDTO = encuentroParticipanteRepo.findByEncuentroIdEncuentro(idEncuentro).stream()
                .map(p -> new ParticipanteJuezDTO(p.getIdReferencia(), p.getTipo(), obtenerNombreParticipante(p.getTipo(), p.getIdReferencia()))).toList();

        return new EncuentroDetalleJuezDTO(encuentro.getIdEncuentro(), encuentro.getEstado(), encuentro.getTipo(), encuentro.getRonda(), participantesDTO);
    }

    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================
    private String obtenerNombreParticipante(TipoParticipante tipo, String idReferencia) {
        if (tipo == TipoParticipante.ROBOT) {
            return robotRepo.findById(idReferencia).map(Robot::getNombre).orElse(idReferencia);
        }
        return equipoRepo.findById(idReferencia).map(EquipoTorneo::getNombre).orElse(idReferencia);
    }

    private List<Encuentro> generarEncuentrosInterno(CrearEncuentrosDTO dto) {
        CategoriaTorneo categoria = categoriaRepo.findById(dto.getIdCategoriaTorneo()).orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        Juez juez = juezRepo.findById(dto.getIdJuez()).orElseThrow(() -> new RuntimeException("Juez no encontrado"));
        Coliseo coliseo = coliseoRepo.findById(dto.getIdColiseo()).orElseThrow(() -> new RuntimeException("Coliseo no encontrado"));

        List<String> participantes = obtenerParticipantes(categoria);
        if (participantes.isEmpty()) throw new RuntimeException("No hay participantes activos.");

        if (dto.getTipoEncuentro() == TipoEncuentro.ELIMINACION_DIRECTA) {
            return generarEliminacionDirecta(categoria, juez, coliseo, participantes);
        }
        return generarTodosContraTodos(categoria, juez, coliseo, participantes);
    }

    private List<String> obtenerParticipantes(CategoriaTorneo categoria) {
        if (categoria.getModalidad() == ModalidadCategoria.INDIVIDUAL) {
            return inscripcionRepo.findByCategoriaTorneoIdCategoriaTorneoAndEstado(categoria.getIdCategoriaTorneo(), EstadoInscripcion.ACTIVADA)
                    .stream().filter(i -> i.getRobot() != null).map(i -> i.getRobot().getIdRobot()).collect(Collectors.toList());
        } else {
            return equipoRepo.findByCategoriaTorneoIdCategoriaTorneo(categoria.getIdCategoriaTorneo())
                    .stream().map(EquipoTorneo::getIdEquipo).filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    private List<Encuentro> generarEliminacionDirecta(CategoriaTorneo categoria, Juez juez, Coliseo coliseo, List<String> participantes) {
        if (participantes.size() % 2 != 0) throw new RuntimeException("Se requiere número par.");
        Collections.shuffle(participantes);
        List<Encuentro> encuentros = new ArrayList<>();
        for (int i = 0; i < participantes.size(); i += 2) {
            Encuentro e = crearEncuentroBase(categoria, juez, coliseo, TipoEncuentro.ELIMINACION_DIRECTA);
            crearParticipantes(e, participantes.get(i), participantes.get(i + 1), categoria.getModalidad());
            encuentros.add(e);
        }
        return encuentros;
    }

    private List<Encuentro> generarTodosContraTodos(CategoriaTorneo categoria, Juez juez, Coliseo coliseo, List<String> participantes) {
        List<Encuentro> encuentros = new ArrayList<>();
        for (int i = 0; i < participantes.size(); i++) {
            for (int j = i + 1; j < participantes.size(); j++) {
                Encuentro e = crearEncuentroBase(categoria, juez, coliseo, TipoEncuentro.TODOS_CONTRA_TODOS);
                crearParticipantes(e, participantes.get(i), participantes.get(j), categoria.getModalidad());
                encuentros.add(e);
            }
        }
        return encuentros;
    }

    private Encuentro crearEncuentroBase(CategoriaTorneo cat, Juez juez, Coliseo col, TipoEncuentro tipo) {
        Encuentro encuentro = Encuentro.builder()
                .idEncuentro(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .categoriaTorneo(cat).juez(juez).coliseo(col).tipo(tipo)
                .estado(EstadoEncuentro.PROGRAMADO).ronda(1).fecha(new Date()).build();
        return encuentroRepo.save(encuentro);
    }

    private void crearParticipantes(Encuentro encuentro, String p1, String p2, ModalidadCategoria modalidad) {
        TipoParticipante tipo = (modalidad == ModalidadCategoria.INDIVIDUAL) ? TipoParticipante.ROBOT : TipoParticipante.EQUIPO;
        EncuentroParticipante ep1 = EncuentroParticipante.builder().encuentro(encuentro).tipo(tipo).idReferencia(p1).build();
        EncuentroParticipante ep2 = EncuentroParticipante.builder().encuentro(encuentro).tipo(tipo).idReferencia(p2).build();
        encuentroParticipanteRepo.saveAll(List.of(ep1, ep2));
    }

    private EncuentroAdminDTO toAdminDTO(Encuentro e) {
        String nombreTorneo = (e.getCategoriaTorneo() != null && e.getCategoriaTorneo().getTorneo() != null) ? e.getCategoriaTorneo().getTorneo().getNombre() : "—";
        String nombreCategoria = (e.getCategoriaTorneo() != null) ? e.getCategoriaTorneo().getCategoria().name() : "—";
        String nombreJuez = (e.getJuez() != null && e.getJuez().getUsuario() != null) ? e.getJuez().getUsuario().getNombres() + " " + e.getJuez().getUsuario().getApellidos() : "—";
        return new EncuentroAdminDTO(e.getIdEncuentro(), nombreTorneo, nombreCategoria, e.getTipo(), e.getRonda(), e.getEstado(), nombreJuez, e.getColiseo().getNombre());
    }
}