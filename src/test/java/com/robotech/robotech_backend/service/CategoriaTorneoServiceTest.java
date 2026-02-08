package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CategoriaTorneoDTO;
import com.robotech.robotech_backend.dto.CategoriaTorneoPublicoDTO;
import com.robotech.robotech_backend.model.entity.CategoriaTorneo;
import com.robotech.robotech_backend.model.entity.EquipoTorneo;
import com.robotech.robotech_backend.model.entity.InscripcionTorneo;
import com.robotech.robotech_backend.model.entity.Torneo;
import com.robotech.robotech_backend.model.enums.CategoriaCompetencia;
import com.robotech.robotech_backend.model.enums.EstadoEquipoTorneo;
import com.robotech.robotech_backend.model.enums.EstadoInscripcion;
import com.robotech.robotech_backend.model.enums.ModalidadCategoria;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.HistorialCalificacionRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import com.robotech.robotech_backend.repository.TorneoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaTorneoServiceTest {

    @Mock private CategoriaTorneoRepository repo;
    @Mock private TorneoRepository torneoRepo;
    @Mock private HistorialCalificacionRepository historialRepo;
    @Mock private InscripcionTorneoRepository inscripcionRepo;
    @Mock private EquipoTorneoRepository equipoRepo;

    @InjectMocks
    private CategoriaTorneoService service;

    @Test
    void crear_individual_ok() {
        Torneo torneo = Torneo.builder().idTorneo("T1").build();
        CategoriaTorneoDTO dto = new CategoriaTorneoDTO();
        dto.setCategoria(CategoriaCompetencia.MINISUMO);
        dto.setModalidad(ModalidadCategoria.INDIVIDUAL);
        dto.setMaxParticipantes(8);

        when(torneoRepo.findById("T1")).thenReturn(Optional.of(torneo));
        when(repo.save(any(CategoriaTorneo.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaTorneo created = service.crear("T1", dto);

        assertEquals(ModalidadCategoria.INDIVIDUAL, created.getModalidad());
        assertEquals(8, created.getMaxParticipantes());
    }

    @Test
    void crear_equipo_sin_max_throws() {
        Torneo torneo = Torneo.builder().idTorneo("T1").build();
        CategoriaTorneoDTO dto = new CategoriaTorneoDTO();
        dto.setCategoria(CategoriaCompetencia.MINISUMO);
        dto.setModalidad(ModalidadCategoria.EQUIPO);

        when(torneoRepo.findById("T1")).thenReturn(Optional.of(torneo));

        assertThrows(IllegalArgumentException.class, () -> service.crear("T1", dto));
    }

    @Test
    void listarPublicoPorTorneo_marca_cerradas_por_fecha() {
        Torneo torneo = Torneo.builder()
                .fechaCierreInscripcion(new Date(System.currentTimeMillis() - 1000))
                .build();
        CategoriaTorneo cat = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT1")
                .torneo(torneo)
                .categoria(CategoriaCompetencia.MINISUMO)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .build();

        when(repo.findByTorneoIdTorneo("T1")).thenReturn(List.of(cat));

        List<CategoriaTorneoPublicoDTO> result = service.listarPublicoPorTorneo("T1");

        assertEquals(true, result.get(0).getInscripcionesCerradas());
    }

    @Test
    void eliminar_individual_anula_inscripciones() {
        CategoriaTorneo cat = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT1")
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .build();
        InscripcionTorneo ins = InscripcionTorneo.builder()
                .estado(EstadoInscripcion.ACTIVADA)
                .build();

        when(repo.findById("CAT1")).thenReturn(Optional.of(cat));
        when(historialRepo.findByEncuentro_CategoriaTorneo_IdCategoriaTorneo("CAT1")).thenReturn(List.of());
        when(inscripcionRepo.findByCategoriaTorneoIdCategoriaTorneo("CAT1")).thenReturn(List.of(ins));

        String msg = service.eliminar("CAT1");

        verify(inscripcionRepo, times(1)).saveAll(any(List.class));
        verify(repo, times(1)).deleteById("CAT1");
        assertEquals(true, msg.contains("Inscripciones anuladas"));
    }
}
