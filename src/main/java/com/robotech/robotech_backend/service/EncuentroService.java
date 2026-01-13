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
    // MÉTODO PÚBLICO (DESDE CONTROLLER)
    // =====================================================
    public List<EncuentroAdminDTO> generarEncuentros(CrearEncuentrosDTO dto) {
        List<Encuentro> encuentros = generarEncuentrosInterno(dto);
        return encuentros.stream().map(this::toAdminDTO).toList();
    }

    // =====================================================
    // MÉTODO INTERNO REAL
    // =====================================================
    private List<Encuentro> generarEncuentrosInterno(CrearEncuentrosDTO dto) {

        CategoriaTorneo categoria = categoriaRepo.findById(dto.getIdCategoriaTorneo())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Juez juez = juezRepo.buscarJuezAprobado(dto.getIdJuez(), EstadoValidacion.APROBADO)
                .orElseThrow(() -> new RuntimeException("Juez no aprobado"));

        Coliseo coliseo = coliseoRepo.findById(dto.getIdColiseo())
                .orElseThrow(() -> new RuntimeException("Coliseo no encontrado"));

        List<String> participantes = obtenerParticipantes(categoria);

        if (participantes.isEmpty()) {
            throw new RuntimeException("No hay participantes inscritos");
        }

        if (dto.getTipoEncuentro() == TipoEncuentro.ELIMINACION_DIRECTA) {
            return generarEliminacionDirecta(categoria, juez, coliseo, participantes);
        }

        return generarTodosContraTodos(categoria, juez, coliseo, participantes);
    }

    // =====================================================
    // OBTENER PARTICIPANTES
    // =====================================================
    private List<String> obtenerParticipantes(CategoriaTorneo categoria) {

        if (categoria.getModalidad() == ModalidadCategoria.INDIVIDUAL) {
            return inscripcionRepo
                    .findByCategoriaTorneoIdCategoriaTorneoAndEstado(
                            categoria.getIdCategoriaTorneo(),
                            EstadoInscripcion.ACTIVA
                    )
                    .stream()
                    .map(i -> i.getRobot() != null ? i.getRobot().getIdRobot() : null)
                    .filter(Objects::nonNull)
                    .toList();
        }

        return equipoRepo
                .findByCategoriaTorneoIdCategoriaTorneo(categoria.getIdCategoriaTorneo())
                .stream()
                .map(EquipoTorneo::getIdEquipo)
                .filter(Objects::nonNull)
                .toList();
    }

    // =====================================================
    // ELIMINACIÓN DIRECTA
    // =====================================================
    private List<Encuentro> generarEliminacionDirecta(
            CategoriaTorneo categoria,
            Juez juez,
            Coliseo coliseo,
            List<String> participantes
    ) {

        validarParticipantes(participantes, true);
        Collections.shuffle(participantes);

        List<Encuentro> encuentros = new ArrayList<>();

        for (int i = 0; i < participantes.size(); i += 2) {
            // Como ya validaste que es par, i+1 es seguro
            String p1 = participantes.get(i);
            String p2 = participantes.get(i + 1);

            if (p1 == null || p2 == null) {
                throw new RuntimeException("Participante inválido");
            }

            Encuentro encuentro = crearEncuentroBase(
                    categoria,
                    juez,
                    coliseo,
                    TipoEncuentro.ELIMINACION_DIRECTA
            );

            crearParticipantes(
                    encuentro,
                    p1,
                    p2,
                    categoria.getModalidad()
            );

            encuentros.add(encuentro);
        }

        return encuentros;
    }

    // =====================================================
    // TODOS CONTRA TODOS
    // =====================================================
    private List<Encuentro> generarTodosContraTodos(
            CategoriaTorneo categoria,
            Juez juez,
            Coliseo coliseo,
            List<String> participantes
    ) {

        validarParticipantes(participantes, false);

        List<Encuentro> encuentros = new ArrayList<>();

        for (int i = 0; i < participantes.size(); i++) {
            for (int j = i + 1; j < participantes.size(); j++) {

                String p1 = participantes.get(i);
                String p2 = participantes.get(j);

                if (p1 == null || p2 == null) {
                    throw new RuntimeException("Participante inválido");
                }

                Encuentro encuentro = crearEncuentroBase(
                        categoria,
                        juez,
                        coliseo,
                        TipoEncuentro.TODOS_CONTRA_TODOS
                );

                crearParticipantes(encuentro, p1, p2, categoria.getModalidad());

                encuentros.add(encuentro);
            }
        }

        return encuentros;
    }

    // =====================================================
    // VALIDACIONES / UTILIDADES
    // =====================================================
    private void validarParticipantes(List<String> participantes, boolean requierePar) {

        if (participantes.size() < 2) {
            throw new RuntimeException("No hay suficientes participantes");
        }

        if (requierePar && participantes.size() % 2 != 0) {
            throw new RuntimeException("La cantidad de participantes debe ser par");
        }
    }

    private Encuentro crearEncuentroBase(
            CategoriaTorneo categoria,
            Juez juez,
            Coliseo coliseo,
            TipoEncuentro tipo
    ) {

        Encuentro encuentro = Encuentro.builder()
                .idEncuentro(
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase()
                )
                .categoriaTorneo(categoria)
                .juez(juez)
                .coliseo(coliseo)
                .tipo(tipo)
                .estado(EstadoEncuentro.PROGRAMADO)
                .ronda(1)
                .build();

        return encuentroRepo.save(encuentro);
    }

    private void crearParticipantes(
            Encuentro encuentro,
            String participante1,
            String participante2,
            ModalidadCategoria modalidad
    ) {

        TipoParticipante tipoParticipante =
                modalidad == ModalidadCategoria.INDIVIDUAL
                        ? TipoParticipante.ROBOT
                        : TipoParticipante.EQUIPO;

        EncuentroParticipante ep1 = EncuentroParticipante.builder()
                .encuentro(encuentro)
                .tipo(tipoParticipante)
                .idReferencia(participante1)
                .build();

        EncuentroParticipante ep2 = EncuentroParticipante.builder()
                .encuentro(encuentro)
                .tipo(tipoParticipante)
                .idReferencia(participante2)
                .build();

        encuentroParticipanteRepo.saveAll(List.of(ep1, ep2));
    }

    // =====================================================
    // REGISTRAR RESULTADO (JUEZ ASIGNADO)
    // =====================================================
    public Encuentro registrarResultado(String idJuez, RegistrarResultadoEncuentroDTO dto) {

        Encuentro encuentro = encuentroRepo.findById(dto.getIdEncuentro())
                .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));

        if (!encuentro.getJuez().getIdJuez().equals(idJuez)) {
            throw new RuntimeException("No autorizado para calificar este encuentro");
        }

        if (encuentro.getEstado() == EstadoEncuentro.FINALIZADO) {
            throw new RuntimeException("El encuentro ya fue calificado");
        }

        List<EncuentroParticipante> participantes =
                encuentroParticipanteRepo.findByEncuentroIdEncuentro(encuentro.getIdEncuentro());

        if (participantes.isEmpty()) {
            throw new RuntimeException("El encuentro no tiene participantes");
        }

        if (dto.getIdGanador() == null || dto.getIdGanador().isBlank()) {
            throw new RuntimeException("Debe indicar el ganador");
        }

        EncuentroParticipante ganador = participantes.stream()
                .filter(p -> p.getIdReferencia().equals(dto.getIdGanador()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Ganador inválido"));

        Map<String, CalificacionParticipanteDTO> calificaciones = Collections.emptyMap();
        if (dto.getCalificaciones() != null && !dto.getCalificaciones().isEmpty()) {
            calificaciones = dto.getCalificaciones()
                    .stream()
                    .collect(Collectors.toMap(
                            CalificacionParticipanteDTO::getIdReferencia,
                            Function.identity(),
                            (actual, reemplazo) -> reemplazo
                    ));

            for (String idReferencia : calificaciones.keySet()) {
                boolean existe = participantes.stream()
                        .anyMatch(p -> p.getIdReferencia().equals(idReferencia));
                if (!existe) {
                    throw new RuntimeException("Calificación inválida");
                }
            }
        }

        for (EncuentroParticipante participante : participantes) {
            CalificacionParticipanteDTO calificacion = calificaciones.get(participante.getIdReferencia());
            if (calificacion != null) {
                participante.setCalificacion(calificacion.getCalificacion());
            }
            participante.setGanador(participante.getIdReferencia().equals(ganador.getIdReferencia()));
        }

        encuentro.setGanadorIdReferencia(ganador.getIdReferencia());
        encuentro.setGanadorTipo(ganador.getTipo());
        encuentro.setEstado(EstadoEncuentro.FINALIZADO);

        encuentroParticipanteRepo.saveAll(participantes);
        return encuentroRepo.save(encuentro);
    }

    // =====================================================
    // DTO ADMIN (NULL-SAFE)
    // =====================================================
    private EncuentroAdminDTO toAdminDTO(Encuentro e) {

        String torneoNombre = "—";
        String categoriaNombre = "—";

        if (e.getCategoriaTorneo() != null) {
            if (e.getCategoriaTorneo().getTorneo() != null &&
                    e.getCategoriaTorneo().getTorneo().getNombre() != null) {
                torneoNombre = e.getCategoriaTorneo().getTorneo().getNombre();
            }
            if (e.getCategoriaTorneo().getCategoria() != null) {
                categoriaNombre = e.getCategoriaTorneo().getCategoria().name();
            }
        }

        TipoEncuentro tipo = e.getTipo() != null ? e.getTipo() : TipoEncuentro.ELIMINACION_DIRECTA;
        Integer ronda = e.getRonda() != null ? e.getRonda() : 1;
        EstadoEncuentro estado = e.getEstado() != null ? e.getEstado() : EstadoEncuentro.PROGRAMADO;

        String juez = "—";
        if (e.getJuez() != null && e.getJuez().getUsuario() != null) {
            juez = e.getJuez().getUsuario().getCorreo() != null
                    ? e.getJuez().getUsuario().getCorreo()
                    : "—";
        }

        String coliseo = e.getColiseo() != null && e.getColiseo().getNombre() != null
                ? e.getColiseo().getNombre()
                : "—";

        return new EncuentroAdminDTO(
                e.getIdEncuentro(),
                torneoNombre,
                categoriaNombre,
                tipo,
                ronda,
                estado,
                juez,
                coliseo
        );
    }
}
