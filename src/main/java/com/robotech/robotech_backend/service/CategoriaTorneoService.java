package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CategoriaTorneoDTO;
import com.robotech.robotech_backend.model.Torneo;
import com.robotech.robotech_backend.model.CategoriaTorneo;
import com.robotech.robotech_backend.repository.TorneoRepository;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaTorneoService {

    private final CategoriaTorneoRepository repo;
    private final TorneoRepository torneoRepo;

    public List<CategoriaTorneo> listarPorTorneo(String idTorneo) {
        return repo.findByTorneoIdTorneo(idTorneo);
    }

    public CategoriaTorneo crear(String idTorneo, CategoriaTorneoDTO dto) {

        Torneo torneo = torneoRepo.findById(idTorneo)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        CategoriaTorneo c = CategoriaTorneo.builder()
                .torneo(torneo)
                .categoria(dto.getCategoria())
                .maxParticipantes(dto.getMaxParticipantes())
                .descripcion(dto.getDescripcion())
                .build();

        return repo.save(c);
    }

    public CategoriaTorneo editar(String idCategoria, CategoriaTorneoDTO dto) {

        CategoriaTorneo c = repo.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        c.setCategoria(dto.getCategoria());
        c.setMaxParticipantes(dto.getMaxParticipantes());
        c.setDescripcion(dto.getDescripcion());

        return repo.save(c);
    }

    public void eliminar(String idCategoria) {
        repo.deleteById(idCategoria);
    }
}
