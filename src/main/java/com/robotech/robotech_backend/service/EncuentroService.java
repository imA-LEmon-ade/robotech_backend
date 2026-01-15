package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CalificacionParticipanteDTO;
import com.robotech.robotech_backend.dto.CrearEncuentrosDTO;
import com.robotech.robotech_backend.dto.EncuentroAdminDTO;
import com.robotech.robotech_backend.dto.RegistrarResultadoEncuentroDTO;
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
    public Encuentro registrarResultado(String idJuez, RegistrarResultadoEncuentroDTO dto) {

        Encuentro encuentro = encuentroRepo.findById(dto.getIdEncuentro())
                .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));

        // Validar que el juez asignado es el que está intentando registrar
        if (!encuentro.getJuez().getIdJuez().equals(idJuez)) {
            throw new RuntimeException("No tiene autorización para calificar este encuentro.");
        }

        if (encuentro.getEstado() == EstadoEncuentro.FINALIZADO) {
            throw new RuntimeException("El encuentro ya fue finalizado previamente.");
        }

        List<EncuentroParticipante> participantes =
                encuentroParticipanteRepo.findByEncuentroIdEncuentro(encuentro.getIdEncuentro());

        if (participantes.isEmpty()) {
            throw new RuntimeException("Error crítico: El encuentro no tiene participantes registrados.");
        }

        // Validar Ganador
        if (dto.getIdGanador() == null || dto.getIdGanador().isBlank()) {
            throw new RuntimeException("Debe seleccionar un ganador.");
        }

        EncuentroParticipante ganador = participantes.stream()
                .filter(p -> p.getIdReferencia().equals(dto.getIdGanador()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El ID del ganador no corresponde a ningún participante de este encuentro."));

        // Procesar Calificaciones (Puntajes) si existen
        Map<String, CalificacionParticipanteDTO> mapaCalificaciones = Collections.emptyMap();
        if (dto.getCalificaciones() != null && !dto.getCalificaciones().isEmpty()) {
            mapaCalificaciones = dto.getCalificaciones().stream()
                    .collect(Collectors.toMap(CalificacionParticipanteDTO::getIdReferencia, Function.identity()));
        }

        // Actualizar Participantes
        for (EncuentroParticipante participante : participantes) {
            // Asignar puntaje si viene en el DTO
            if (mapaCalificaciones.containsKey(participante.getIdReferencia())) {
                participante.setCalificacion(mapaCalificaciones.get(participante.getIdReferencia()).getCalificacion());
            }

            // Marcar si es ganador o no
            boolean esGanador = participante.getIdReferencia().equals(ganador.getIdReferencia());
            participante.setGanador(esGanador);
        }

        // Actualizar Encuentro
        encuentro.setGanadorIdReferencia(ganador.getIdReferencia());
        encuentro.setGanadorTipo(ganador.getTipo());
        encuentro.setEstado(EstadoEncuentro.FINALIZADO);

        // Guardar cambios
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
}