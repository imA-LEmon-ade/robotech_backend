package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ClubPublicoDTO;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubPublicoService {

    private final ClubRepository clubRepo;
    private final CompetidorRepository competidorRepo;

    public List<ClubPublicoDTO> obtenerClubesParaPublico() {
        return clubRepo.findAll().stream().map(club -> {
            // Contamos cuántos competidores están asociados a este club
            long conteo = competidorRepo.countByClubActual_IdClub(club.getIdClub());

            return new ClubPublicoDTO(
                    club.getIdClub(),
                    club.getNombre(),
                    conteo
            );
        }).toList();
    }
}