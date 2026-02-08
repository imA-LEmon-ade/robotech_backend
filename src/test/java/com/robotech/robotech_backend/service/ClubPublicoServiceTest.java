package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ClubPublicoDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubPublicoServiceTest {

    @Mock private ClubRepository clubRepo;
    @Mock private CompetidorRepository competidorRepo;

    @InjectMocks
    private ClubPublicoService clubPublicoService;

    @Test
    void obtenerClubesParaPublico_mapea_conteo() {
        Club club = Club.builder()
                .idClub("C1")
                .nombre("Robotech")
                .direccionFiscal("Av 123")
                .correoContacto("club@robotech.com")
                .build();

        when(clubRepo.findAll()).thenReturn(List.of(club));
        when(competidorRepo.countByClubActual_IdClub("C1")).thenReturn(4L);

        List<ClubPublicoDTO> result = clubPublicoService.obtenerClubesParaPublico();

        assertEquals(1, result.size());
        assertEquals("Robotech", result.get(0).getNombre());
        assertEquals(4L, result.get(0).getCantidadCompetidores());
    }
}
