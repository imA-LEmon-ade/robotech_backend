package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.TransferenciaPropietarioCrearDTO;
import com.robotech.robotech_backend.dto.TransferenciaPropietarioDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.TransferenciaPropietarioRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferenciaPropietarioService {

    private final TransferenciaPropietarioRepository transferenciaRepo;
    private final ClubRepository clubRepo;
    private final CompetidorRepository competidorRepo;
    private final UsuarioRepository usuarioRepo;

    public TransferenciaPropietarioDTO solicitar(String idUsuarioClub, TransferenciaPropietarioCrearDTO dto) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        if (dto == null || dto.getIdCompetidor() == null || dto.getIdCompetidor().isBlank()) {
            throw new RuntimeException("Selecciona un competidor");
        }

        if (transferenciaRepo.existsByClub_IdClubAndEstado(club.getIdClub(), EstadoTransferenciaPropietario.PENDIENTE)) {
            throw new RuntimeException("Ya hay una solicitud pendiente");
        }

        Competidor comp = competidorRepo.findById(dto.getIdCompetidor())
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        if (comp.getClubActual() == null || !comp.getClubActual().getIdClub().equals(club.getIdClub())) {
            throw new RuntimeException("El competidor no pertenece a tu club");
        }

        if (comp.getEstadoValidacion() != EstadoValidacion.APROBADO) {
            throw new RuntimeException("El competidor no est? aprobado");
        }

        if (comp.getUsuario() == null || comp.getUsuario().getIdUsuario().equals(club.getUsuario().getIdUsuario())) {
            throw new RuntimeException("El competidor ya es propietario del club");
        }

        TransferenciaPropietario t = TransferenciaPropietario.builder()
                .club(club)
                .propietarioActual(club.getUsuario())
                .competidorNuevo(comp)
                .estado(EstadoTransferenciaPropietario.PENDIENTE)
                .build();

        return toDTO(transferenciaRepo.save(t));
    }

    public List<TransferenciaPropietarioDTO> listarPorClub(String idUsuarioClub) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        return transferenciaRepo.findByClub_IdClubOrderByCreadoEnDesc(club.getIdClub())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public TransferenciaPropietarioDTO cancelar(String idUsuarioClub, String idTransferencia) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        TransferenciaPropietario t = transferenciaRepo.findById(idTransferencia)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!t.getClub().getIdClub().equals(club.getIdClub())) {
            throw new RuntimeException("No puedes cancelar esta solicitud");
        }

        if (t.getEstado() != EstadoTransferenciaPropietario.PENDIENTE) {
            throw new RuntimeException("La solicitud no se puede cancelar");
        }

        t.setEstado(EstadoTransferenciaPropietario.CANCELADA);
        return toDTO(transferenciaRepo.save(t));
    }

    public List<TransferenciaPropietarioDTO> listarPendientes() {
        return transferenciaRepo.findByEstado(EstadoTransferenciaPropietario.PENDIENTE)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public TransferenciaPropietarioDTO aprobar(String idTransferencia) {
        TransferenciaPropietario t = transferenciaRepo.findById(idTransferencia)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (t.getEstado() != EstadoTransferenciaPropietario.PENDIENTE) {
            throw new RuntimeException("La solicitud no est? pendiente");
        }

        Club club = t.getClub();
        if (club == null) {
            throw new RuntimeException("Club no encontrado");
        }

        Usuario propietarioActual = t.getPropietarioActual();
        if (propietarioActual == null || club.getUsuario() == null
                || !club.getUsuario().getIdUsuario().equals(propietarioActual.getIdUsuario())) {
            throw new RuntimeException("El propietario actual ya cambi?");
        }

        Competidor compNuevo = t.getCompetidorNuevo();
        if (compNuevo == null || compNuevo.getUsuario() == null) {
            throw new RuntimeException("Competidor no v?lido");
        }

        if (compNuevo.getClubActual() == null
                || !compNuevo.getClubActual().getIdClub().equals(club.getIdClub())) {
            throw new RuntimeException("El competidor ya no pertenece al club");
        }

        if (compNuevo.getEstadoValidacion() != EstadoValidacion.APROBADO) {
            throw new RuntimeException("El competidor no est? aprobado");
        }

        Usuario nuevoOwner = compNuevo.getUsuario();
        if (nuevoOwner.getIdUsuario().equals(propietarioActual.getIdUsuario())) {
            throw new RuntimeException("El competidor ya es propietario del club");
        }

        Optional<Club> clubDelNuevoOwner = clubRepo.findByUsuario(nuevoOwner);
        if (clubDelNuevoOwner.isPresent() && !clubDelNuevoOwner.get().getIdClub().equals(club.getIdClub())) {
            throw new RuntimeException("El usuario ya es propietario de otro club");
        }

        propietarioActual.getRoles().remove(RolUsuario.CLUB);
        propietarioActual.getRoles().add(RolUsuario.COMPETIDOR);

        nuevoOwner.getRoles().add(RolUsuario.CLUB);
        nuevoOwner.getRoles().add(RolUsuario.COMPETIDOR);

        club.setUsuario(nuevoOwner);

        usuarioRepo.save(propietarioActual);
        usuarioRepo.save(nuevoOwner);
        clubRepo.save(club);

        t.setEstado(EstadoTransferenciaPropietario.APROBADA);
        t.setAprobadoEn(new Date());

        return toDTO(transferenciaRepo.save(t));
    }

    public TransferenciaPropietarioDTO rechazar(String idTransferencia) {
        TransferenciaPropietario t = transferenciaRepo.findById(idTransferencia)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (t.getEstado() != EstadoTransferenciaPropietario.PENDIENTE) {
            throw new RuntimeException("La solicitud no est? pendiente");
        }

        t.setEstado(EstadoTransferenciaPropietario.RECHAZADA);
        return toDTO(transferenciaRepo.save(t));
    }

    private TransferenciaPropietarioDTO toDTO(TransferenciaPropietario t) {
        String nombrePropietario = null;
        String correoPropietario = null;
        if (t.getPropietarioActual() != null) {
            nombrePropietario = (t.getPropietarioActual().getNombres() != null ? t.getPropietarioActual().getNombres() : "")
                    + " "
                    + (t.getPropietarioActual().getApellidos() != null ? t.getPropietarioActual().getApellidos() : "");
            nombrePropietario = nombrePropietario.trim();
            correoPropietario = t.getPropietarioActual().getCorreo();
        }

        String nombreCompetidor = null;
        String correoCompetidor = null;
        if (t.getCompetidorNuevo() != null && t.getCompetidorNuevo().getUsuario() != null) {
            Usuario u = t.getCompetidorNuevo().getUsuario();
            nombreCompetidor = (u.getNombres() != null ? u.getNombres() : "")
                    + " "
                    + (u.getApellidos() != null ? u.getApellidos() : "");
            nombreCompetidor = nombreCompetidor.trim();
            correoCompetidor = u.getCorreo();
        }

        return TransferenciaPropietarioDTO.builder()
                .idTransferencia(t.getIdTransferencia())
                .idClub(t.getClub() != null ? t.getClub().getIdClub() : null)
                .nombreClub(t.getClub() != null ? t.getClub().getNombre() : null)
                .idPropietarioActual(t.getPropietarioActual() != null ? t.getPropietarioActual().getIdUsuario() : null)
                .nombrePropietarioActual(nombrePropietario)
                .correoPropietarioActual(correoPropietario)
                .idCompetidorNuevo(t.getCompetidorNuevo() != null ? t.getCompetidorNuevo().getIdCompetidor() : null)
                .nombreCompetidorNuevo(nombreCompetidor)
                .correoCompetidorNuevo(correoCompetidor)
                .estado(t.getEstado())
                .creadoEn(t.getCreadoEn())
                .actualizadoEn(t.getActualizadoEn())
                .aprobadoEn(t.getAprobadoEn())
                .build();
    }
}


