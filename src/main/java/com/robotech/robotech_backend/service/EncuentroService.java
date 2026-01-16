package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.*;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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
    // 1. GENERAR ENCUENTROS (MÉTODO PÚBLICO)
    // =====================================================
    public List<EncuentroAdminDTO> generarEncuentros(CrearEncuentrosDTO dto) {
        System.out.println("⚡ INICIANDO GENERACIÓN DE ENCUENTROS");

        try {
            List<Encuentro> encuentros = generarEncuentrosInterno(dto);
            System.out.println("✅ Generación exitosa. Cantidad: " + encuentros.size());

            return encuentros.stream()
                    .map(this::toAdminDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("❌ ERROR AL GENERAR ENCUENTROS: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // =====================================================
    // 2. REGISTRAR RESULTADO (MÉTODO PÚBLICO - JUEZ)
    // =====================================================
    @Transactional
    public Encuentro registrarResultado(String idJuez, RegistrarResultadoEncuentroDTO dto) {

        Encuentro encuentro = encuentroRepo.findById(dto.getIdEncuentro())
                .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));

        // 1️⃣ Validar juez asignado
        if (!encuentro.getJuez().getIdJuez().equals(idJuez)) {
            throw new RuntimeException("No tiene autorización para calificar este encuentro.");
        }

        // 2️⃣ Validar estado
        if (encuentro.getEstado() == EstadoEncuentro.FINALIZADO) {
            throw new RuntimeException("El encuentro ya fue finalizado previamente.");
        }

        List<EncuentroParticipante> participantes =
                encuentroParticipanteRepo.findByEncuentroIdEncuentro(encuentro.getIdEncuentro());

        if (participantes.isEmpty()) {
            throw new RuntimeException("Error crítico: El encuentro no tiene participantes registrados.");
        }

        // 3️⃣ Validar calificaciones
        if (dto.getCalificaciones() == null || dto.getCalificaciones().isEmpty()) {
            throw new RuntimeException("Debe registrar los puntajes de los participantes.");
        }

        Map<String, Integer> puntajes = dto.getCalificaciones().stream()
                .collect(Collectors.toMap(
                        CalificacionParticipanteDTO::getIdReferencia,
                        CalificacionParticipanteDTO::getCalificacion
                ));

        // 4️⃣ Asignar puntajes + validar rango
        for (EncuentroParticipante participante : participantes) {

            Integer calificacion = puntajes.get(participante.getIdReferencia());

            if (calificacion == null) {
                throw new RuntimeException(
                        "Falta puntaje para el participante " + participante.getIdReferencia()
                );
            }

            if (calificacion < 0 || calificacion > 100) {
                throw new RuntimeException(
                        "El puntaje debe estar entre 0 y 100"
                );
            }

            participante.setCalificacion(calificacion);
        }

        // 5️⃣ Calcular ganador (mayor puntaje)
        int maxPuntaje = participantes.stream()
                .mapToInt(EncuentroParticipante::getCalificacion)
                .max()
                .orElseThrow();

        List<EncuentroParticipante> ganadores = participantes.stream()
                .filter(p -> p.getCalificacion() == maxPuntaje)
                .toList();

        // 6️⃣ Validar empate
        if (ganadores.size() > 1) {
            throw new RuntimeException(
                    "Empate detectado. No se puede definir un ganador."
            );
        }

        EncuentroParticipante ganador = ganadores.get(0);

        // 7️⃣ Marcar ganador + guardar historial
        for (EncuentroParticipante participante : participantes) {

            boolean esGanador = participante.getIdReferencia()
                    .equals(ganador.getIdReferencia());

            participante.setGanador(esGanador);

            historialRepo.save(
                    HistorialCalificacion.builder()
                            .encuentro(encuentro)
                            .tipo(participante.getTipo())
                            .idReferencia(participante.getIdReferencia())
                            .puntaje(participante.getCalificacion())
                            .build()
            );
        }

        // 8️⃣ Actualizar encuentro
        encuentro.setGanadorIdReferencia(ganador.getIdReferencia());
        encuentro.setGanadorTipo(ganador.getTipo());
        encuentro.setEstado(EstadoEncuentro.FINALIZADO);

        // 9️⃣ Persistir
        encuentroParticipanteRepo.saveAll(participantes);
        return encuentroRepo.save(encuentro);
    }


    // =====================================================
    // LÓGICA INTERNA: GENERACIÓN
    // =====================================================
    private List<Encuentro> generarEncuentrosInterno(CrearEncuentrosDTO dto) {

        CategoriaTorneo categoria = categoriaRepo.findById(dto.getIdCategoriaTorneo())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Juez juez = juezRepo.findById(dto.getIdJuez())
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        if (juez.getEstadoValidacion() != EstadoValidacion.APROBADO) {
            throw new RuntimeException("El juez seleccionado no está APROBADO.");
        }

        Coliseo coliseo = coliseoRepo.findById(dto.getIdColiseo())
                .orElseThrow(() -> new RuntimeException("Coliseo no encontrado"));

        List<String> participantes = obtenerParticipantes(categoria);

        if (participantes.isEmpty()) {
            throw new RuntimeException("No hay participantes inscritos activos.");
        }

        if (dto.getTipoEncuentro() == TipoEncuentro.ELIMINACION_DIRECTA) {
            return generarEliminacionDirecta(categoria, juez, coliseo, participantes);
        }

        return generarTodosContraTodos(categoria, juez, coliseo, participantes);
    }

    private List<String> obtenerParticipantes(CategoriaTorneo categoria) {
        if (categoria.getModalidad() == ModalidadCategoria.INDIVIDUAL) {
            return inscripcionRepo.findByCategoriaTorneoIdCategoriaTorneoAndEstado(
                            categoria.getIdCategoriaTorneo(), EstadoInscripcion.ACTIVA)
                    .stream()
                    .filter(i -> i.getRobot() != null)
                    .map(i -> i.getRobot().getIdRobot())
                    .collect(Collectors.toList());
        } else {
            return equipoRepo.findByCategoriaTorneoIdCategoriaTorneo(categoria.getIdCategoriaTorneo())
                    .stream()
                    .map(EquipoTorneo::getIdEquipo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    // =====================================================
    // ALGORITMOS DE EMPAREJAMIENTO
    // =====================================================
    private List<Encuentro> generarEliminacionDirecta(
            CategoriaTorneo categoria, Juez juez, Coliseo coliseo, List<String> participantes) {

        if (participantes.size() % 2 != 0) {
            throw new RuntimeException("Número impar de participantes (" + participantes.size() + "). Se requiere un número PAR para eliminación directa.");
        }

        Collections.shuffle(participantes);
        List<Encuentro> encuentros = new ArrayList<>();

        for (int i = 0; i < participantes.size(); i += 2) {
            String p1 = participantes.get(i);
            String p2 = participantes.get(i + 1);
            Encuentro e = crearEncuentroBase(categoria, juez, coliseo, TipoEncuentro.ELIMINACION_DIRECTA);
            crearParticipantes(e, p1, p2, categoria.getModalidad());
            encuentros.add(e);
        }
        return encuentros;
    }

    private List<Encuentro> generarTodosContraTodos(
            CategoriaTorneo categoria, Juez juez, Coliseo coliseo, List<String> participantes) {

        if (participantes.size() < 2) {
            throw new RuntimeException("Se requieren al menos 2 participantes.");
        }

        List<Encuentro> encuentros = new ArrayList<>();
        for (int i = 0; i < participantes.size(); i++) {
            for (int j = i + 1; j < participantes.size(); j++) {
                String p1 = participantes.get(i);
                String p2 = participantes.get(j);
                Encuentro e = crearEncuentroBase(categoria, juez, coliseo, TipoEncuentro.TODOS_CONTRA_TODOS);
                crearParticipantes(e, p1, p2, categoria.getModalidad());
                encuentros.add(e);
            }
        }
        return encuentros;
    }

    // =====================================================
    // UTILIDADES PRIVADAS
    // =====================================================
    private Encuentro crearEncuentroBase(CategoriaTorneo cat, Juez juez, Coliseo col, TipoEncuentro tipo) {
        Encuentro encuentro = Encuentro.builder()
                .idEncuentro(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .categoriaTorneo(cat)
                .juez(juez)
                .coliseo(col)
                .tipo(tipo)
                .estado(EstadoEncuentro.PROGRAMADO)
                .ronda(1)
                .fecha(new Date()) // ✅ ESTO FUNCIONARÁ PORQUE YA ACTUALIZASTE LA ENTIDAD
                .build();
        return encuentroRepo.save(encuentro);
    }

    private void crearParticipantes(Encuentro encuentro, String p1, String p2, ModalidadCategoria modalidad) {
        TipoParticipante tipo = (modalidad == ModalidadCategoria.INDIVIDUAL) ? TipoParticipante.ROBOT : TipoParticipante.EQUIPO;

        EncuentroParticipante ep1 = EncuentroParticipante.builder()
                .encuentro(encuentro).tipo(tipo).idReferencia(p1).build();
        EncuentroParticipante ep2 = EncuentroParticipante.builder()
                .encuentro(encuentro).tipo(tipo).idReferencia(p2).build();

        encuentroParticipanteRepo.saveAll(List.of(ep1, ep2));
    }

    private EncuentroAdminDTO toAdminDTO(Encuentro e) {
        String nombreTorneo = "—";
        String nombreCategoria = "—";
        String nombreJuez = "—";
        String nombreColiseo = "—";

        if (e.getCategoriaTorneo() != null) {
            if (e.getCategoriaTorneo().getCategoria() != null) nombreCategoria = e.getCategoriaTorneo().getCategoria().name();
            if (e.getCategoriaTorneo().getTorneo() != null) nombreTorneo = e.getCategoriaTorneo().getTorneo().getNombre();
        }
        if (e.getJuez() != null && e.getJuez().getUsuario() != null) {
            nombreJuez = e.getJuez().getUsuario().getNombres() + " " + e.getJuez().getUsuario().getApellidos();
        }
        if (e.getColiseo() != null) {
            nombreColiseo = e.getColiseo().getNombre();
        }

        return new EncuentroAdminDTO(
                e.getIdEncuentro(),
                nombreTorneo,
                nombreCategoria,
                e.getTipo(),
                e.getRonda(),
                e.getEstado(),
                nombreJuez,
                nombreColiseo
        );
    }

    public List<EncuentroJuezDTO> listarEncuentrosPorJuez(String idJuez) {

        List<Encuentro> encuentros = encuentroRepo.findByJuezIdJuez(idJuez);

        return encuentros.stream().map(encuentro -> {

            List<EncuentroParticipante> participantes =
                    encuentroParticipanteRepo.findByEncuentroIdEncuentro(
                            encuentro.getIdEncuentro()
                    );

            List<ParticipanteEncuentroDTO> participantesDTO =
                    participantes.stream().map(p -> ParticipanteEncuentroDTO.builder()
                            .idReferencia(p.getIdReferencia())
                            .tipo(p.getTipo())
                            .calificacion(p.getCalificacion())
                            .ganador(p.getGanador())
                            .build()
                    ).toList();

            return EncuentroJuezDTO.builder()
                    .idEncuentro(encuentro.getIdEncuentro())
                    .categoria(encuentro.getCategoriaTorneo().getCategoria().name())
                    .tipo(encuentro.getTipo())
                    .estado(encuentro.getEstado())
                    .coliseo(encuentro.getColiseo().getNombre())
                    .fecha(encuentro.getFecha())
                    .participantes(participantesDTO)
                    .build();

        }).toList();
    }

    public EncuentroDetalleJuezDTO obtenerDetalleParaJuez(
            String idUsuario,
            String idEncuentro
    ) {

        Juez juez = juezRepo.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        Encuentro encuentro = encuentroRepo.findById(idEncuentro)
                .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));

        // 🔐 Validar que el encuentro pertenece al juez
        if (!encuentro.getJuez().getIdJuez().equals(juez.getIdJuez())) {
            throw new RuntimeException("No autorizado para ver este encuentro");
        }

        List<EncuentroParticipante> participantes =
                encuentroParticipanteRepo.findByEncuentroIdEncuentro(idEncuentro);

        List<ParticipanteJuezDTO> participantesDTO = participantes.stream()
                .map(p -> new ParticipanteJuezDTO(
                        p.getIdReferencia(),
                        p.getTipo(),
                        obtenerNombreParticipante(p.getTipo(), p.getIdReferencia())
                ))
                .toList();

        return new EncuentroDetalleJuezDTO(
                encuentro.getIdEncuentro(),
                encuentro.getEstado(),
                encuentro.getTipo(),
                encuentro.getRonda(),
                participantesDTO
        );
    }


    private String obtenerNombreParticipante(
            TipoParticipante tipo,
            String idReferencia
    ) {
        if (tipo == TipoParticipante.ROBOT) {
            return robotRepo.findById(idReferencia)
                    .map(Robot::getNombre)
                    .orElse(idReferencia);
        }

        return equipoRepo.findById(idReferencia)
                .map(EquipoTorneo::getNombre)
                .orElse(idReferencia);
    }

}