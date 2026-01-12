package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CategoriaEncuentroAdminDTO;
import com.robotech.robotech_backend.model.CategoriaTorneo;
import com.robotech.robotech_backend.model.EstadoEquipoTorneo;
import com.robotech.robotech_backend.model.EstadoInscripcion;
import com.robotech.robotech_backend.model.ModalidadCategoria;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEncuentrosService {

    private final CategoriaTorneoRepository categoriaRepo;
    private final InscripcionTorneoRepository inscripcionRepo;
    private final EquipoTorneoRepository equipoRepo;

    // ----------------------------------------------------
    // LISTAR CATEGORÍAS PARA GESTIÓN DE ENCUENTROS
    // ----------------------------------------------------
    public List<CategoriaEncuentroAdminDTO> listarCategoriasActivas() {

        Date hoy = new Date();

        return categoriaRepo.findAll().stream()
                .map(categoria -> mapearCategoria(categoria, hoy))
                .toList();
    }

    // ----------------------------------------------------
    // MAPPER INTERNO (limpio y mantenible)
    // ----------------------------------------------------
    private CategoriaEncuentroAdminDTO mapearCategoria(
            CategoriaTorneo categoria,
            Date hoy
    ) {

        boolean inscripcionesCerradas =
                hoy.after(categoria.getTorneo().getFechaCierreInscripcion());

        int inscritos;
        int max;

        if (categoria.getModalidad() == ModalidadCategoria.INDIVIDUAL) {

            inscritos = (int) inscripcionRepo
                    .countByCategoriaTorneoIdCategoriaTorneoAndEstado(
                            categoria.getIdCategoriaTorneo(),
                            EstadoInscripcion.ACTIVA
                    );

            max = categoria.getMaxParticipantes();

        } else {

            inscritos = (int) equipoRepo
                    .findByCategoriaTorneoIdCategoriaTorneo(
                            categoria.getIdCategoriaTorneo()
                    )
                    .stream()
                    .filter(equipo -> equipo.getEstado() == EstadoEquipoTorneo.APROBADO)
                    .count();

            max = categoria.getMaxEquipos();
        }

        return CategoriaEncuentroAdminDTO.builder()
                .idCategoriaTorneo(categoria.getIdCategoriaTorneo())
                .categoria(categoria.getCategoria().name())
                .modalidad(categoria.getModalidad().name())
                .inscritos(inscritos)
                .maxParticipantes(max)
                .torneo(categoria.getTorneo().getNombre())
                .idTorneo(categoria.getTorneo().getIdTorneo())
                .inscripcionesCerradas(inscripcionesCerradas)
                .build();
    }
}
