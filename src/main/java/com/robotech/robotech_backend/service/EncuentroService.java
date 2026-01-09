package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearEncuentrosDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
    public List<Encuentro> generarEncuentros(CrearEncuentrosDTO dto) {

        Juez juez = juezRepo.findById(dto.getIdJuez())
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        Coliseo coliseo = coliseoRepo.findById(dto.getIdColiseo())
                .orElseThrow(() -> new RuntimeException("Coliseo no encontrado"));

        return generarEncuentrosInterno(
                dto.getIdCategoriaTorneo(),
                dto.getTipoEncuentro(),
                juez,
                coliseo
        );
    }

    // =====================================================
    // MÉTODO INTERNO REAL
    // =====================================================
    private List<Encuentro> generarEncuentrosInterno(
            String idCategoriaTorneo,
            TipoEncuentro tipo,
            Juez juez,
            Coliseo coliseo
    ) {

        CategoriaTorneo categoria = categoriaRepo.findById(idCategoriaTorneo)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        List<String> participantes = obtenerParticipantes(categoria);

        if (tipo == TipoEncuentro.ELIMINACION_DIRECTA) {
            return generarEliminacionDirecta(categoria, juez, coliseo, participantes);
        }

        if (tipo == TipoEncuentro.TODOS_CONTRA_TODOS) {
            return generarTodosContraTodos(categoria, juez, coliseo, participantes);
        }

        throw new RuntimeException("Tipo de encuentro no soportado");
    }

    // =====================================================
    // OBTENER PARTICIPANTES
    // =====================================================
    private List<String> obtenerParticipantes(CategoriaTorneo categoria) {

        // INDIVIDUAL → ROBOTS ACTIVOS
        if (categoria.getModalidad() == ModalidadCategoria.INDIVIDUAL) {
            return inscripcionRepo
                    .findByCategoriaTorneoIdCategoriaTorneoAndEstado(
                            categoria.getIdCategoriaTorneo(),
                            EstadoInscripcion.ACTIVA
                    )
                    .stream()
                    .map(i -> i.getRobot().getIdRobot())
                    .toList();
        }

        // EQUIPO → TODOS LOS EQUIPOS INSCRITOS
        return equipoRepo
                .findByCategoriaTorneoIdCategoriaTorneo(
                        categoria.getIdCategoriaTorneo()
                )
                .stream()
                .map(EquipoTorneo::getIdEquipo)
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

            Encuentro encuentro = crearEncuentroBase(
                    categoria,
                    juez,
                    coliseo,
                    TipoEncuentro.ELIMINACION_DIRECTA
            );

            crearParticipantes(
                    encuentro,
                    participantes.get(i),
                    participantes.get(i + 1),
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

                Encuentro encuentro = crearEncuentroBase(
                        categoria,
                        juez,
                        coliseo,
                        TipoEncuentro.TODOS_CONTRA_TODOS
                );

                crearParticipantes(
                        encuentro,
                        participantes.get(i),
                        participantes.get(j),
                        categoria.getModalidad()
                );

                encuentros.add(encuentro);
            }
        }

        return encuentros;
    }

    // =====================================================
    // UTILIDADES
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
                .categoriaTorneo(categoria)
                .juez(juez)
                .coliseo(coliseo)
                .tipo(tipo)
                .estado(EstadoEncuentro.PROGRAMADO)
                .build();

        return encuentroRepo.save(encuentro);
    }

    private void crearParticipantes(
            Encuentro encuentro,
            String participante1,
            String participante2,
            ModalidadCategoria modalidad
    ) {

        EncuentroParticipante ep1 = EncuentroParticipante.builder()
                .id(UUID.randomUUID().toString())
                .encuentro(encuentro)
                .tipo(modalidad.name())
                .idReferencia(participante1)
                .build();

        EncuentroParticipante ep2 = EncuentroParticipante.builder()
                .id(UUID.randomUUID().toString())
                .encuentro(encuentro)
                .tipo(modalidad.name())
                .idReferencia(participante2)
                .build();

        encuentroParticipanteRepo.saveAll(List.of(ep1, ep2));
    }
}
