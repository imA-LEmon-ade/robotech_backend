package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CategoriaEncuentroAdminDTO;
import com.robotech.robotech_backend.model.entity.CategoriaTorneo;
import com.robotech.robotech_backend.model.entity.Torneo;
import com.robotech.robotech_backend.model.enums.CategoriaCompetencia;
import com.robotech.robotech_backend.model.enums.EstadoInscripcion;
import com.robotech.robotech_backend.model.enums.ModalidadCategoria;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.EncuentroRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminEncuentrosServiceTest {

    @Mock private CategoriaTorneoRepository categoriaRepo;
    @Mock private InscripcionTorneoRepository inscripcionRepo;
    @Mock private EquipoTorneoRepository equipoRepo;
    @Mock private EncuentroRepository encuentroRepo;

    @InjectMocks
    private AdminEncuentrosService adminEncuentrosService;

    @Test
    void listarCategoriasActivas_individual_mapea_inscritos() {
        Torneo torneo = Torneo.builder()
                .idTorneo("T1")
                .nombre("Torneo Nacional")
                .fechaCierreInscripcion(new Date(System.currentTimeMillis() - 1000))
                .build();

        CategoriaTorneo categoria = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT1")
                .torneo(torneo)
                .categoria(CategoriaCompetencia.MINISUMO)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .maxParticipantes(8)
                .build();

        when(categoriaRepo.findAll()).thenReturn(List.of(categoria));
        when(inscripcionRepo.countByCategoriaTorneoIdCategoriaTorneoAndEstado("CAT1", EstadoInscripcion.ACTIVADA))
                .thenReturn(3L);
        when(encuentroRepo.existsByCategoriaTorneoIdCategoriaTorneo("CAT1")).thenReturn(true);

        List<CategoriaEncuentroAdminDTO> result = adminEncuentrosService.listarCategoriasActivas();

        assertEquals(1, result.size());
        CategoriaEncuentroAdminDTO dto = result.get(0);
        assertEquals("CAT1", dto.getIdCategoriaTorneo());
        assertEquals("Torneo Nacional", dto.getTorneo());
        assertEquals(3, dto.getInscritos());
        assertEquals(8, dto.getMaxParticipantes());
        assertTrue(dto.isInscripcionesCerradas());
        assertTrue(dto.isHasEncuentros());
    }

    @Test
    void listarCategoriasActivas_filtra_por_term_y_estado() {
        Torneo torneo1 = Torneo.builder()
                .idTorneo("T1")
                .nombre("Torneo Nacional")
                .fechaCierreInscripcion(new Date(System.currentTimeMillis() + 100000))
                .build();
        Torneo torneo2 = Torneo.builder()
                .idTorneo("T2")
                .nombre("Regional")
                .fechaCierreInscripcion(new Date(System.currentTimeMillis() + 100000))
                .build();

        CategoriaTorneo cat1 = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT1")
                .torneo(torneo1)
                .categoria(CategoriaCompetencia.MINISUMO)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .maxParticipantes(8)
                .build();

        CategoriaTorneo cat2 = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT2")
                .torneo(torneo2)
                .categoria(CategoriaCompetencia.MICROSUMO)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .maxParticipantes(8)
                .build();

        when(categoriaRepo.findAll()).thenReturn(List.of(cat1, cat2));
        when(inscripcionRepo.countByCategoriaTorneoIdCategoriaTorneoAndEstado(anyString(), any(EstadoInscripcion.class)))
                .thenReturn(0L);
        when(encuentroRepo.existsByCategoriaTorneoIdCategoriaTorneo(anyString())).thenReturn(false);

        List<CategoriaEncuentroAdminDTO> result = adminEncuentrosService.listarCategoriasActivas("nacional", "ABIERTAS");

        assertEquals(1, result.size());
        assertEquals("Torneo Nacional", result.get(0).getTorneo());
    }
}
