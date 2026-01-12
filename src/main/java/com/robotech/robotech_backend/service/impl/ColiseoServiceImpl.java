package com.robotech.robotech_backend.service.impl;

import com.robotech.robotech_backend.dto.ColiseoDTO;
import com.robotech.robotech_backend.model.Coliseo;
import com.robotech.robotech_backend.repository.ColiseoRepository;
import com.robotech.robotech_backend.service.ColiseoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColiseoServiceImpl implements ColiseoService {

    private final ColiseoRepository coliseoRepository;

    @Override
    public ColiseoDTO crear(ColiseoDTO dto) {

        Coliseo c = new Coliseo();
        c.setIdColiseo(UUID.randomUUID().toString().substring(0, 8));
        c.setNombre(dto.getNombre());
        c.setUbicacion(dto.getUbicacion());

        coliseoRepository.save(c);
        return toDTO(c);
    }

    @Override
    public List<ColiseoDTO> listar() {
        return coliseoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public ColiseoDTO editar(String id, ColiseoDTO dto) {

        Coliseo c = coliseoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coliseo no encontrado"));

        c.setNombre(dto.getNombre());
        c.setUbicacion(dto.getUbicacion());

        coliseoRepository.save(c);
        return toDTO(c);
    }

    @Override
    public void eliminar(String id) {
        coliseoRepository.deleteById(id);
    }

    // =========================
    // MAPPER
    // =========================
    private ColiseoDTO toDTO(Coliseo c) {
        ColiseoDTO dto = new ColiseoDTO();
        dto.setIdColiseo(c.getIdColiseo());
        dto.setNombre(c.getNombre());
        dto.setUbicacion(c.getUbicacion());
        dto.setImagenUrl(c.getImagenUrl()); //
        return dto;
    }


}
