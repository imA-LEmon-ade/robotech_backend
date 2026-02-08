package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.*;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
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

    // =====================================================
    // 1. GENERAR ENCUENTROS
    // =====================================================
    public List<EncuentroAdminDTO> generarEncuentros(CrearEncuentrosDTO dto) {
        try {
            List<Encuentro> encuentros = generarEncuentrosInterno(dto, false);
            return encuentros.stream()
                    .map(this::toAdminDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("? ERROR AL GENERAR ENCUENTROS: " + e.getMessage());
            throw e;
        }
    }

    public List<EncuentroAdminDTO> regenerarEncuentros(CrearEncuentrosDTO dto) {
        try {
            encuentroRepo.deleteByCategoriaTorneoIdCategoriaTorneo(dto.getIdCategoriaTorneo());
            encuentroRepo.flush();
            List<Encuentro> encuentros = generarEncuentrosInterno(dto, true);
            return encuentros.stream()
                    .map(this::toAdminDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("? ERROR AL REGENERAR ENCUENTROS: " + e.getMessage());
            throw e;
        }
    }


    // =====================================================
    // 1.1 LISTAR ENCUENTROS POR CATEGORIA (ADMIN)
    // =====================================================
    public List<EncuentroAdminDTO> listarEncuentrosAdminPorCategoria(String idCategoriaTorneo) {
        return encuentroRepo.findByCategoriaTorneoIdCategoriaTorneo(idCategoriaTorneo)
                .stream()
                .map(this::toAdminDTO)
                .toList();
    }

    // =====================================================
    // 1.2 ACTUALIZAR ENCUENTRO (ADMIN)
    // =====================================================
    public EncuentroAdminDTO actualizarEncuentroAdmin(String idEncuentro, ActualizarEncuentroAdminDTO dto) {
        Encuentro encuentro = encuentroRepo.findById(idEncuentro)
                .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));

        if (encuentro.getEstado() == EstadoEncuentro.FINALIZADO) {
            throw new RuntimeException("No se puede editar un encuentro finalizado");
        }

        if (dto.getIdJuez() != null && !dto.getIdJuez().isBlank()) {
            Juez juez = juezRepo.findById(dto.getIdJuez())
                    .orElseThrow(() -> new RuntimeException("Juez no encontrado"));
            encuentro.setJuez(juez);
        }

        if (dto.getIdColiseo() != null && !dto.getIdColiseo().isBlank()) {
            Coliseo coliseo = coliseoRepo.findById(dto.getIdColiseo())
                    .orElseThrow(() -> new RuntimeException("Coliseo no encontrado"));
            encuentro.setColiseo(coliseo);
        }

        if (dto.getRonda() != null) {
            encuentro.setRonda(dto.getRonda());
        }

        if (dto.getFecha() != null) {
            encuentro.setFecha(dto.getFecha());
        }

        if (dto.getEstado() != null) {
            encuentro.setEstado(dto.getEstado());
        }

        Encuentro guardado = encuentroRepo.save(encuentro);
        return toAdminDTO(guardado);
    }

    // =====================================================
    // 2. REGISTRAR RESULTADO
    // =====================================================
    @Transactional
    public Encuentro registrarResultado(String idJuez, RegistrarResultadoEncuentroDTO dto) {
        Encuentro encuentro = encuentroRepo.findById(dto.getIdEncuentro())
                .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));

        if (!encuentro.getJuez().getIdJuez().equals(idJuez)) {
            throw new RuntimeException("No tiene autorizaci?n para calificar este encuentro.");
        }

        validarJuezNoParticipaEnEncuentro(encuentro, idJuez);

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

        generarSiguienteRondaSiCorresponde(guardado);
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
    // 4. VISTA P?BLICA (BRACKET / LIGA)
    // =====================================================
    public List<EncuentroPublicoDTO> listarEncuentrosPublicosPorCategoria(String idCategoriaTorneo) {
        return encuentroRepo.findByCategoriaTorneoIdCategoriaTorneo(idCategoriaTorneo).stream().map(encuentro -> {
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

            String nombreTorneo = (encuentro.getCategoriaTorneo() != null && encuentro.getCategoriaTorneo().getTorneo() != null)
                    ? encuentro.getCategoriaTorneo().getTorneo().getNombre() : "-";
            String nombreCategoria = (encuentro.getCategoriaTorneo() != null && encuentro.getCategoriaTorneo().getCategoria() != null)
                    ? encuentro.getCategoriaTorneo().getCategoria().name() : "-";

            return EncuentroPublicoDTO.builder()
                    .idEncuentro(encuentro.getIdEncuentro())
                    .torneo(nombreTorneo)
                    .categoria(nombreCategoria)
                    .tipo(encuentro.getTipo())
                    .estado(encuentro.getEstado())
                    .ronda(encuentro.getRonda())
                    .fecha(encuentro.getFecha())
                    .coliseo(encuentro.getColiseo() != null ? encuentro.getColiseo().getNombre() : "-")
                    .participantes(participantesDTO)
                    .build();
        }).toList();
    }

    // =====================================================
    // M?TODOS AUXILIARES
    // =====================================================
    private String obtenerNombreParticipante(TipoParticipante tipo, String idReferencia) {
        if (tipo == TipoParticipante.ROBOT) {
            return robotRepo.findById(idReferencia).map(Robot::getNombre).orElse(idReferencia);
        }
        return equipoRepo.findById(idReferencia).map(EquipoTorneo::getNombre).orElse(idReferencia);
    }


    private List<Encuentro> generarEncuentrosInterno(CrearEncuentrosDTO dto, boolean permitirRecrear) {
        CategoriaTorneo categoria = categoriaRepo.findById(dto.getIdCategoriaTorneo()).orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
        Juez juez = juezRepo.findById(dto.getIdJuez()).orElseThrow(() -> new RuntimeException("Juez no encontrado"));
        Coliseo coliseo = coliseoRepo.findById(dto.getIdColiseo()).orElseThrow(() -> new RuntimeException("Coliseo no encontrado"));

        if (!permitirRecrear && encuentroRepo.existsByCategoriaTorneoIdCategoriaTorneo(categoria.getIdCategoriaTorneo())) {
            throw new RuntimeException("Ya se generaron encuentros para esta categoria");
        }

        List<String> participantes = obtenerParticipantes(categoria);
        validarJuezNoParticipa(juez, categoria, participantes);
        if (participantes.isEmpty()) throw new RuntimeException("No hay participantes activos.");

        if (dto.getTipoEncuentro() == TipoEncuentro.ELIMINACION_DIRECTA) {
            return generarEliminacionDirecta(categoria, juez, coliseo, participantes);
        }
        return generarTodosContraTodos(categoria, juez, coliseo, participantes);
    }

    private List<String> obtenerParticipantes(CategoriaTorneo categoria) {
        if (categoria.getModalidad() == ModalidadCategoria.INDIVIDUAL) {
            List<InscripcionTorneo> inscripciones = inscripcionRepo
                    .findByCategoriaTorneoIdCategoriaTorneoAndEstado(
                            categoria.getIdCategoriaTorneo(),
                            EstadoInscripcion.ACTIVADA
                    );

            List<String> invalidos = new ArrayList<>();
            List<String> participantes = new ArrayList<>();

            for (InscripcionTorneo i : inscripciones) {
                Robot r = i.getRobot();
                if (r == null || r.getCompetidor() == null) {
                    invalidos.add(i.getIdInscripcion());
                    continue;
                }
                if (r.getEstado() != EstadoRobot.ACTIVO) {
                    invalidos.add(r.getIdRobot());
                    continue;
                }
                if (r.getCompetidor().getEstadoValidacion() != EstadoValidacion.APROBADO) {
                    invalidos.add(r.getIdRobot());
                    continue;
                }
                participantes.add(r.getIdRobot());
            }

            if (!invalidos.isEmpty()) {
                throw new RuntimeException(
                        "Hay participantes no aprobados o inactivos: " + String.join(", ", invalidos)
                );
            }

            return participantes;
        } else {
            List<EquipoTorneo> equipos = equipoRepo.findByCategoriaTorneoIdCategoriaTorneo(
                    categoria.getIdCategoriaTorneo()
            );

            List<String> invalidos = new ArrayList<>();
            List<String> participantes = new ArrayList<>();

            for (EquipoTorneo e : equipos) {
                if (e.getEstado() == EstadoEquipoTorneo.ANULADA ||
                        e.getEstado() == EstadoEquipoTorneo.RECHAZADO) {
                    continue;
                }
                if (e.getRobots() == null || e.getRobots().isEmpty()) {
                    invalidos.add(e.getIdEquipo());
                    continue;
                }

                boolean equipoValido = true;
                for (Robot r : e.getRobots()) {
                    if (r == null || r.getCompetidor() == null) {
                        equipoValido = false;
                        break;
                    }
                    if (r.getEstado() != EstadoRobot.ACTIVO) {
                        equipoValido = false;
                        break;
                    }
                    if (r.getCompetidor().getEstadoValidacion() != EstadoValidacion.APROBADO) {
                        equipoValido = false;
                        break;
                    }
                }

                if (!equipoValido) {
                    invalidos.add(e.getIdEquipo());
                    continue;
                }
                if (e.getIdEquipo() != null) {
                    participantes.add(e.getIdEquipo());
                }
            }

            if (!invalidos.isEmpty()) {
                throw new RuntimeException(
                        "Hay equipos con robots no aprobados o inactivos: " + String.join(", ", invalidos)
                );
            }

            return participantes;
        }
    }

    private List<Encuentro> generarEliminacionDirecta(CategoriaTorneo categoria, Juez juez, Coliseo coliseo, List<String> participantes) {
        if (participantes.size() % 2 != 0) {
            throw new RuntimeException("No se puede generar eliminacion directa con numero impar de inscritos: "
                    + participantes.size() + ". Debe ser par.");
        }
        Collections.shuffle(participantes);
        List<Encuentro> encuentros = new ArrayList<>();
        for (int i = 0; i < participantes.size(); i += 2) {
            Encuentro e = crearEncuentroBase(categoria, juez, coliseo, TipoEncuentro.ELIMINACION_DIRECTA, 1);
            crearParticipantes(e, participantes.get(i), participantes.get(i + 1), categoria.getModalidad());
            encuentros.add(e);
        }
        return encuentros;
    }

    private List<Encuentro> generarTodosContraTodos(CategoriaTorneo categoria, Juez juez, Coliseo coliseo, List<String> participantes) {
        List<String> lista = new ArrayList<>(participantes);
        if (lista.size() % 2 != 0) {
            // Agregamos un BYE para poder distribuir por rondas sin repetir rivales
            lista.add(null);
        }

        int n = lista.size();
        int rounds = n - 1;
        int half = n / 2;
        List<Encuentro> encuentros = new ArrayList<>();

        for (int r = 1; r <= rounds; r++) {
            for (int i = 0; i < half; i++) {
                String p1 = lista.get(i);
                String p2 = lista.get(n - 1 - i);
                if (p1 == null || p2 == null) {
                    continue; // BYE
                }
                Encuentro e = crearEncuentroBase(categoria, juez, coliseo, TipoEncuentro.TODOS_CONTRA_TODOS, r);
                crearParticipantes(e, p1, p2, categoria.getModalidad());
                encuentros.add(e);
            }

            // Rotaci?n tipo "circle method": fija el primero y rota el resto
            String last = lista.remove(n - 1);
            lista.add(1, last);
        }

        return encuentros;
    }

    private Encuentro crearEncuentroBase(CategoriaTorneo cat, Juez juez, Coliseo col, TipoEncuentro tipo, int ronda) {
        Encuentro encuentro = Encuentro.builder()
                .idEncuentro(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .categoriaTorneo(cat).juez(juez).coliseo(col).tipo(tipo)
                .estado(EstadoEncuentro.PROGRAMADO).ronda(ronda).fecha(new Date()).build();
        return encuentroRepo.save(encuentro);
    }

    private void crearParticipantes(Encuentro encuentro, String p1, String p2, ModalidadCategoria modalidad) {
        TipoParticipante tipo = (modalidad == ModalidadCategoria.INDIVIDUAL) ? TipoParticipante.ROBOT : TipoParticipante.EQUIPO;
        EncuentroParticipante ep1 = EncuentroParticipante.builder().encuentro(encuentro).tipo(tipo).idReferencia(p1).build();
        EncuentroParticipante ep2 = EncuentroParticipante.builder().encuentro(encuentro).tipo(tipo).idReferencia(p2).build();
        encuentroParticipanteRepo.saveAll(List.of(ep1, ep2));
    }

    private EncuentroAdminDTO toAdminDTO(Encuentro e) {
        String nombreTorneo = (e.getCategoriaTorneo() != null && e.getCategoriaTorneo().getTorneo() != null) ? e.getCategoriaTorneo().getTorneo().getNombre() : "-";
        String nombreCategoria = (e.getCategoriaTorneo() != null) ? e.getCategoriaTorneo().getCategoria().name() : "-";
        String nombreJuez = (e.getJuez() != null && e.getJuez().getUsuario() != null) ? e.getJuez().getUsuario().getNombres() + " " + e.getJuez().getUsuario().getApellidos() : "-";
        return new EncuentroAdminDTO(e.getIdEncuentro(), nombreTorneo, nombreCategoria, e.getTipo(), e.getRonda(), e.getEstado(), nombreJuez, e.getColiseo().getNombre());
    }

    private void generarSiguienteRondaSiCorresponde(Encuentro encuentroFinalizado) {
        if (encuentroFinalizado.getTipo() != TipoEncuentro.ELIMINACION_DIRECTA) {
            return;
        }

        Integer rondaActual = encuentroFinalizado.getRonda();
        if (rondaActual == null || rondaActual < 1) {
            return;
        }

        String idCategoriaTorneo = encuentroFinalizado.getCategoriaTorneo().getIdCategoriaTorneo();
        int siguienteRonda = rondaActual + 1;

        if (encuentroRepo.existsByCategoriaTorneoIdCategoriaTorneoAndTipoAndRonda(
                idCategoriaTorneo,
                TipoEncuentro.ELIMINACION_DIRECTA,
                siguienteRonda
        )) {
            return; // ya existe, no recrear
        }

        List<Encuentro> rondaActualEncuentros = encuentroRepo
                .findByCategoriaTorneoIdCategoriaTorneoAndTipoAndRonda(
                        idCategoriaTorneo,
                        TipoEncuentro.ELIMINACION_DIRECTA,
                        rondaActual
                );

        if (rondaActualEncuentros.isEmpty()) {
            return;
        }

        boolean todosFinalizados = rondaActualEncuentros.stream()
                .allMatch(e -> e.getEstado() == EstadoEncuentro.FINALIZADO);
        if (!todosFinalizados) {
            return;
        }

        List<String> ganadores = new ArrayList<>();
        for (Encuentro e : rondaActualEncuentros) {
            if (e.getGanadorIdReferencia() == null) {
                return;
            }
            ganadores.add(e.getGanadorIdReferencia());
        }

        if (ganadores.size() <= 1) {
            return; // torneo finalizado
        }

        if (ganadores.size() % 2 != 0) {
            throw new RuntimeException("No se puede generar la siguiente ronda con n?mero impar de ganadores");
        }

        for (int i = 0; i < ganadores.size(); i += 2) {
            Encuentro nuevo = crearEncuentroBase(
                    encuentroFinalizado.getCategoriaTorneo(),
                    encuentroFinalizado.getJuez(),
                    encuentroFinalizado.getColiseo(),
                    TipoEncuentro.ELIMINACION_DIRECTA,
                    siguienteRonda
            );
            crearParticipantes(nuevo, ganadores.get(i), ganadores.get(i + 1), encuentroFinalizado.getCategoriaTorneo().getModalidad());
        }
    }

    private void validarJuezNoParticipa(Juez juez, CategoriaTorneo categoria, List<String> participantes) {
        if (juez == null || juez.getUsuario() == null) {
            return;
        }
        if (categoria == null || categoria.getModalidad() != ModalidadCategoria.INDIVIDUAL) {
            return;
        }

        List<Robot> robots = robotRepo.findByCompetidor_IdCompetidor(juez.getUsuario().getIdUsuario());
        if (robots == null || robots.isEmpty()) {
            return;
        }

        for (Robot robot : robots) {
            if (participantes.contains(robot.getIdRobot())) {
                throw new RuntimeException("El juez asignado participa en este torneo. Selecciona otro juez.");
            }
        }
    }

    private void validarJuezNoParticipaEnEncuentro(Encuentro encuentro, String idJuez) {
        if (encuentro == null || encuentro.getJuez() == null) {
            return;
        }
        if (!encuentro.getJuez().getIdJuez().equals(idJuez)) {
            return;
        }

        if (encuentro.getCategoriaTorneo() == null || encuentro.getCategoriaTorneo().getModalidad() != ModalidadCategoria.INDIVIDUAL) {
            return;
        }

        List<Robot> robots = robotRepo.findByCompetidor_IdCompetidor(encuentro.getJuez().getUsuario().getIdUsuario());
        if (robots == null || robots.isEmpty()) {
            return;
        }

        Set<String> robotsPropios = robots.stream().map(Robot::getIdRobot).collect(Collectors.toSet());
        List<EncuentroParticipante> participantes = encuentroParticipanteRepo.findByEncuentroIdEncuentro(encuentro.getIdEncuentro());
        for (EncuentroParticipante p : participantes) {
            if (robotsPropios.contains(p.getIdReferencia())) {
                throw new RuntimeException("No puede calificar un encuentro donde participa su robot.");
            }
        }
    }
}


