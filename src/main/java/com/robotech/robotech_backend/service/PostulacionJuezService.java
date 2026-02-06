package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.JuezEstadoDTO;
import com.robotech.robotech_backend.dto.JuezPostulacionDTO;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.model.entity.Juez;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.JuezRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostulacionJuezService {

    private final JuezRepository juezRepo;
    private final UsuarioRepository usuarioRepo;
    private final CompetidorRepository competidorRepo;

    public JuezEstadoDTO obtenerEstado(String idUsuario) {
        Optional<Juez> juez = juezRepo.findByUsuario_IdUsuario(idUsuario);
        if (juez.isEmpty()) {
            return null;
        }
        Juez j = juez.get();
        return new JuezEstadoDTO(j.getIdJuez(), j.getLicencia(), j.getEstadoValidacion());
    }

    public JuezEstadoDTO postular(String idUsuario, JuezPostulacionDTO dto) {
        if (dto == null || dto.getLicencia() == null || dto.getLicencia().isBlank()) {
            throw new RuntimeException("La licencia es obligatoria");
        }

        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Competidor comp = competidorRepo.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        if (comp.getEstadoValidacion() != EstadoValidacion.APROBADO) {
            throw new RuntimeException("El competidor no est? aprobado");
        }

        Optional<Juez> existente = juezRepo.findByUsuario_IdUsuario(idUsuario);
        Optional<Juez> licenciaExistente = juezRepo.findByLicencia(dto.getLicencia());

        if (licenciaExistente.isPresent()) {
            Juez jLic = licenciaExistente.get();
            if (existente.isEmpty() || !jLic.getIdJuez().equals(existente.get().getIdJuez())) {
                throw new RuntimeException("La licencia ya est? registrada");
            }
        }

        if (existente.isPresent()) {
            Juez j = existente.get();
            if (j.getEstadoValidacion() == EstadoValidacion.APROBADO) {
                throw new RuntimeException("Ya eres juez");
            }
            if (j.getEstadoValidacion() == EstadoValidacion.PENDIENTE) {
                throw new RuntimeException("Ya tienes una solicitud pendiente");
            }
            j.setLicencia(dto.getLicencia());
            j.setEstadoValidacion(EstadoValidacion.PENDIENTE);
            j.setCreadoPor(idUsuario);
            j.setCreadoEn(new Date());
            Juez guardado = juezRepo.save(j);
            return new JuezEstadoDTO(guardado.getIdJuez(), guardado.getLicencia(), guardado.getEstadoValidacion());
        }

        Juez juez = Juez.builder()
                .usuario(usuario)
                .licencia(dto.getLicencia())
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .creadoPor(idUsuario)
                .creadoEn(new Date())
                .build();

        Juez guardado = juezRepo.save(juez);
        return new JuezEstadoDTO(guardado.getIdJuez(), guardado.getLicencia(), guardado.getEstadoValidacion());
    }
}


