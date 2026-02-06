package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.SolicitudIngresoCrearDTO;
import com.robotech.robotech_backend.dto.SolicitudIngresoDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudIngresoClubService {

    private final SolicitudIngresoClubRepository solicitudRepo;
    private final CompetidorRepository competidorRepo;
    private final ClubRepository clubRepo;
    private final UsuarioRepository usuarioRepo;
    private final JuezRepository juezRepo;
    private final EncuentroRepository encuentroRepo;

    public SolicitudIngresoDTO solicitar(String idUsuario, SolicitudIngresoCrearDTO dto) {
        if (dto == null || dto.getCodigoClub() == null || dto.getCodigoClub().isBlank()) {
            throw new RuntimeException("Ingresa un c?digo de club");
        }

        Competidor comp = competidorRepo.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        if (comp.getEstadoValidacion() != EstadoValidacion.APROBADO) {
            throw new RuntimeException("Competidor no aprobado");
        }

        if (comp.getClubActual() != null) {
            throw new RuntimeException("Ya perteneces a un club");
        }

        if (solicitudRepo.existsByCompetidor_IdCompetidorAndEstado(comp.getIdCompetidor(), EstadoSolicitudIngresoClub.PENDIENTE)) {
            throw new RuntimeException("Ya tienes una solicitud pendiente");
        }

        Club club = clubRepo.findByCodigoClub(dto.getCodigoClub())
                .orElseThrow(() -> new RuntimeException("C?digo de club inv?lido"));

        if (club.getEstado() != EstadoClub.ACTIVO) {
            throw new RuntimeException("El club est? inactivo");
        }

        SolicitudIngresoClub s = SolicitudIngresoClub.builder()
                .competidor(comp)
                .club(club)
                .estado(EstadoSolicitudIngresoClub.PENDIENTE)
                .build();

        return toDTO(solicitudRepo.save(s));
    }

    public List<SolicitudIngresoDTO> listarMisSolicitudes(String idUsuario) {
        Competidor comp = competidorRepo.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        return solicitudRepo.findByCompetidor_IdCompetidorOrderByCreadoEnDesc(comp.getIdCompetidor())
                .stream().map(this::toDTO).toList();
    }

    public SolicitudIngresoDTO cancelar(String idUsuario, String idSolicitud) {
        Competidor comp = competidorRepo.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        SolicitudIngresoClub s = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!s.getCompetidor().getIdCompetidor().equals(comp.getIdCompetidor())) {
            throw new RuntimeException("No puedes cancelar esta solicitud");
        }

        if (s.getEstado() != EstadoSolicitudIngresoClub.PENDIENTE) {
            throw new RuntimeException("La solicitud no se puede cancelar");
        }

        s.setEstado(EstadoSolicitudIngresoClub.CANCELADA);
        return toDTO(solicitudRepo.save(s));
    }

    public List<SolicitudIngresoDTO> listarPendientesClub(String idUsuarioClub) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        return solicitudRepo.findByClub_IdClubAndEstado(club.getIdClub(), EstadoSolicitudIngresoClub.PENDIENTE)
                .stream().map(this::toDTO).toList();
    }

    public SolicitudIngresoDTO aprobar(String idUsuarioClub, String idSolicitud) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        SolicitudIngresoClub s = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!s.getClub().getIdClub().equals(club.getIdClub())) {
            throw new RuntimeException("Solicitud no corresponde a tu club");
        }

        if (s.getEstado() != EstadoSolicitudIngresoClub.PENDIENTE) {
            throw new RuntimeException("La solicitud no est? pendiente");
        }

        Competidor comp = s.getCompetidor();
        if (comp.getClubActual() != null) {
            throw new RuntimeException("El competidor ya pertenece a un club");
        }

        Usuario usuario = comp.getUsuario();
        if (usuario != null && usuario.getRoles() != null && usuario.getRoles().contains(RolUsuario.JUEZ)) {
            Juez j = juezRepo.findByUsuario_IdUsuario(usuario.getIdUsuario()).orElse(null);
            if (j != null) {
                long pendientes = encuentroRepo.countByJuezIdJuezAndEstadoNot(j.getIdJuez(), EstadoEncuentro.FINALIZADO);
                if (pendientes > 0) {
                    throw new RuntimeException("El juez tiene encuentros pendientes por calificar");
                }
                j.setEstadoValidacion(EstadoValidacion.RECHAZADO);
                j.setValidadoPor("CLUB");
                j.setValidadoEn(new Date());
                juezRepo.save(j);
            }
            usuario.getRoles().remove(RolUsuario.JUEZ);
            usuarioRepo.save(usuario);
        }

        comp.setClubActual(club);
        comp.setEstadoValidacion(EstadoValidacion.APROBADO);
        competidorRepo.save(comp);

        s.setEstado(EstadoSolicitudIngresoClub.APROBADA);
        s.setAprobadoEn(new Date());

        return toDTO(solicitudRepo.save(s));
    }

    public SolicitudIngresoDTO rechazar(String idUsuarioClub, String idSolicitud) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        SolicitudIngresoClub s = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!s.getClub().getIdClub().equals(club.getIdClub())) {
            throw new RuntimeException("Solicitud no corresponde a tu club");
        }

        if (s.getEstado() != EstadoSolicitudIngresoClub.PENDIENTE) {
            throw new RuntimeException("La solicitud no est? pendiente");
        }

        s.setEstado(EstadoSolicitudIngresoClub.RECHAZADA);
        return toDTO(solicitudRepo.save(s));
    }

    private SolicitudIngresoDTO toDTO(SolicitudIngresoClub s) {
        String nombreCompetidor = null;
        String correoCompetidor = null;
        if (s.getCompetidor() != null && s.getCompetidor().getUsuario() != null) {
            Usuario u = s.getCompetidor().getUsuario();
            nombreCompetidor = (u.getNombres() != null ? u.getNombres() : "") + " " +
                    (u.getApellidos() != null ? u.getApellidos() : "");
            nombreCompetidor = nombreCompetidor.trim();
            correoCompetidor = u.getCorreo();
        }

        return SolicitudIngresoDTO.builder()
                .idSolicitud(s.getIdSolicitud())
                .idClub(s.getClub() != null ? s.getClub().getIdClub() : null)
                .nombreClub(s.getClub() != null ? s.getClub().getNombre() : null)
                .idCompetidor(s.getCompetidor() != null ? s.getCompetidor().getIdCompetidor() : null)
                .nombreCompetidor(nombreCompetidor)
                .correoCompetidor(correoCompetidor)
                .estado(s.getEstado())
                .creadoEn(s.getCreadoEn())
                .actualizadoEn(s.getActualizadoEn())
                .aprobadoEn(s.getAprobadoEn())
                .build();
    }
}


