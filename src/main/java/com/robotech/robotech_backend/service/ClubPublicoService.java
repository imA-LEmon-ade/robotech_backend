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
            // 1. Conteo de competidores usando el ID del club
            long conteo = competidorRepo.countByClubActual_IdClub(club.getIdClub());

            // 2. Creación del DTO con los datos directos de la entidad Club
            return new ClubPublicoDTO(
                    club.getIdClub(),
                    club.getNombre(),
                    club.getDireccionFiscal(), // ✅ Campo directo de tu entidad
                    club.getCorreoContacto(),  // ✅ Campo directo de tu entidad
                    conteo
            );
        }).toList();
    }
}