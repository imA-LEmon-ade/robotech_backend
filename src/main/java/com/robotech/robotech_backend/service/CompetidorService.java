package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CompetidorPerfilDTO;
import com.robotech.robotech_backend.dto.RegistroCompetidorDTO;
import com.robotech.robotech_backend.model.CodigoRegistroCompetidor;
import com.robotech.robotech_backend.model.Competidor;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CodigoRegistroCompetidorRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CompetidorService {



    private final CompetidorRepository competidorRepository;

    public CompetidorService(CompetidorRepository competidorRepository) {
        this.competidorRepository = competidorRepository;
    }

    public List<Competidor> listar() { return competidorRepository.findAll(); }

    public Optional<Competidor> obtener(String id) { return competidorRepository.findById(id); }

    public Competidor crear(Competidor competidor) { return competidorRepository.save(competidor); }

    public void eliminar(String id) { competidorRepository.deleteById(id); }

    /*public CompetidorPerfilDTO obtenerPerfil(String idCompetidor) {

        Competidor c = competidorRepository.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        CompetidorPerfilDTO dto = new CompetidorPerfilDTO();
        dto.setIdCompetidor(c.getIdCompetidor());
        dto.setNombres(c.getNombres());
        dto.setApellidos(c.getApellidos());
        dto.setDni(c.getDni());
        dto.setEstadoValidacion(c.getEstadoValidacion());
        dto.setClubActual(c.getClub() != null ? c.getClub().getNombre() : "Sin club");

        // Si luego agregas la foto, úsalo.
        dto.setFotoUrl(c.getUsuario().getFotoUrl());

        // ESTADÍSTICAS
        dto.setTotalRobots(robotRepository.countByCompetidor(c));
        dto.setTotalTorneos(inscripcionRepository.countByCompetidor(c));

        // Si manejas un ranking:
        dto.setPuntosRanking(c.getUsuario().getPuntosRanking());

        return dto;
    }*/



}
