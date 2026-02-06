package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.JuezSelectDTO;
import com.robotech.robotech_backend.model.entity.Juez;
import com.robotech.robotech_backend.repository.JuezRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class JuezService {

    private final JuezRepository juezRepository;

    public JuezService(JuezRepository juezRepository) {
        this.juezRepository = juezRepository;
    }

    public List<Juez> listar() {
        return juezRepository.findAllWithUsuario();
    }

    public Optional<Juez> obtener(String id) {
        return juezRepository.findById(id);
    }

    public Juez crear(Juez juez) {
        return juezRepository.save(juez);
    }

    public void eliminar(String id) {
        juezRepository.deleteById(id);
    }
}


