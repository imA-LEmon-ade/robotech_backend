package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.TransferenciaCrearDTO;
import com.robotech.robotech_backend.dto.TransferenciaDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.TransferenciaCompetidorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferenciaCompetidorService {

    private final TransferenciaCompetidorRepository transferenciaRepo;
    private final CompetidorRepository competidorRepo;
    private final ClubRepository clubRepo;

    public TransferenciaDTO publicar(String idUsuarioClub, TransferenciaCrearDTO dto) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        Competidor competidor = competidorRepo.findById(dto.getIdCompetidor())
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        if (!competidor.getClubActual().getIdClub().equals(club.getIdClub())) {
            throw new RuntimeException("El competidor no pertenece a tu club");
        }

        if (club.getUsuario() != null && competidor.getUsuario() != null
                && club.getUsuario().getIdUsuario().equals(competidor.getUsuario().getIdUsuario())) {
            throw new RuntimeException("No puedes transferir al propietario del club");
        }

        boolean existe = transferenciaRepo.existsByCompetidor_IdCompetidorAndEstadoIn(
                competidor.getIdCompetidor(),
                Set.of(EstadoTransferencia.EN_VENTA, EstadoTransferencia.PENDIENTE)
        );
        if (existe) {
            throw new RuntimeException("El competidor ya tiene una transferencia activa");
        }

        TransferenciaCompetidor t = TransferenciaCompetidor.builder()
                .competidor(competidor)
                .clubOrigen(club)
                .estado(EstadoTransferencia.EN_VENTA)
                .precio(dto.getPrecio())
                .build();

        return toDTO(transferenciaRepo.save(t));
    }

    @Transactional(readOnly = true)
    public List<TransferenciaDTO> listarMercado(String idUsuarioClub) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        return transferenciaRepo.findByEstado(EstadoTransferencia.EN_VENTA).stream()
                .filter(t -> !t.getClubOrigen().getIdClub().equals(club.getIdClub()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TransferenciaDTO solicitar(String idUsuarioClub, String idTransferencia) {
        Club clubDestino = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        TransferenciaCompetidor t = transferenciaRepo.findById(idTransferencia)
                .orElseThrow(() -> new RuntimeException("Transferencia no encontrada"));

        if (t.getEstado() != EstadoTransferencia.EN_VENTA) {
            throw new RuntimeException("La transferencia no está disponible");
        }

        if (t.getClubOrigen().getIdClub().equals(clubDestino.getIdClub())) {
            throw new RuntimeException("No puedes solicitar tu propio competidor");
        }

        t.setClubDestino(clubDestino);
        t.setEstado(EstadoTransferencia.PENDIENTE);
        return toDTO(transferenciaRepo.save(t));
    }

    public TransferenciaDTO aprobar(String idUsuarioClub, String idTransferencia) {
        Club clubOrigen = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        TransferenciaCompetidor t = transferenciaRepo.findById(idTransferencia)
                .orElseThrow(() -> new RuntimeException("Transferencia no encontrada"));

        if (!t.getClubOrigen().getIdClub().equals(clubOrigen.getIdClub())) {
            throw new RuntimeException("No autorizado");
        }

        if (t.getEstado() != EstadoTransferencia.PENDIENTE) {
            throw new RuntimeException("La transferencia no está pendiente");
        }

        if (t.getClubDestino() == null) {
            throw new RuntimeException("No hay club destino asignado");
        }

        Competidor competidor = t.getCompetidor();
        competidor.setClubActual(t.getClubDestino());
        competidorRepo.save(competidor);

        t.setEstado(EstadoTransferencia.APROBADA);
        t.setAprobadoEn(new Date());
        return toDTO(transferenciaRepo.save(t));
    }

    public TransferenciaDTO rechazar(String idUsuarioClub, String idTransferencia) {
        Club clubOrigen = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        TransferenciaCompetidor t = transferenciaRepo.findById(idTransferencia)
                .orElseThrow(() -> new RuntimeException("Transferencia no encontrada"));

        if (!t.getClubOrigen().getIdClub().equals(clubOrigen.getIdClub())) {
            throw new RuntimeException("No autorizado");
        }

        if (t.getEstado() != EstadoTransferencia.PENDIENTE) {
            throw new RuntimeException("La transferencia no está pendiente");
        }

        t.setEstado(EstadoTransferencia.RECHAZADA);
        return toDTO(transferenciaRepo.save(t));
    }

    public TransferenciaDTO cancelar(String idUsuarioClub, String idTransferencia) {
        Club clubOrigen = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        TransferenciaCompetidor t = transferenciaRepo.findById(idTransferencia)
                .orElseThrow(() -> new RuntimeException("Transferencia no encontrada"));

        if (!t.getClubOrigen().getIdClub().equals(clubOrigen.getIdClub())) {
            throw new RuntimeException("No autorizado");
        }

        if (t.getEstado() != EstadoTransferencia.EN_VENTA && t.getEstado() != EstadoTransferencia.PENDIENTE) {
            throw new RuntimeException("La transferencia no se puede cancelar");
        }

        t.setEstado(EstadoTransferencia.CANCELADA);
        return toDTO(transferenciaRepo.save(t));
    }

    @Transactional(readOnly = true)
    public List<TransferenciaDTO> misPublicaciones(String idUsuarioClub) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));
        return transferenciaRepo.findByClubOrigen_IdClub(club.getIdClub()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferenciaDTO> misSolicitudes(String idUsuarioClub) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));
        return transferenciaRepo.findByClubDestino_IdClub(club.getIdClub()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private TransferenciaDTO toDTO(TransferenciaCompetidor t) {
        String nombreCompetidor = "-";
        if (t.getCompetidor() != null && t.getCompetidor().getUsuario() != null) {
            nombreCompetidor = t.getCompetidor().getUsuario().getNombres() + " " + t.getCompetidor().getUsuario().getApellidos();
        }
        return TransferenciaDTO.builder()
                .idTransferencia(t.getIdTransferencia())
                .idCompetidor(t.getCompetidor() != null ? t.getCompetidor().getIdCompetidor() : null)
                .nombreCompetidor(nombreCompetidor)
                .idClubOrigen(t.getClubOrigen() != null ? t.getClubOrigen().getIdClub() : null)
                .nombreClubOrigen(t.getClubOrigen() != null ? t.getClubOrigen().getNombre() : null)
                .idClubDestino(t.getClubDestino() != null ? t.getClubDestino().getIdClub() : null)
                .nombreClubDestino(t.getClubDestino() != null ? t.getClubDestino().getNombre() : null)
                .estado(t.getEstado())
                .precio(t.getPrecio())
                .creadoEn(t.getCreadoEn())
                .actualizadoEn(t.getActualizadoEn())
                .aprobadoEn(t.getAprobadoEn())
                .build();
    }
}


