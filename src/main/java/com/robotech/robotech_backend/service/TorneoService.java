package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearTorneoDTO;
import com.robotech.robotech_backend.model.entity.CategoriaTorneo;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.enums.EstadoEquipoTorneo;
import com.robotech.robotech_backend.model.enums.EstadoInscripcion;
import com.robotech.robotech_backend.model.entity.Torneo;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import com.robotech.robotech_backend.repository.TorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TorneoService {

    private final TorneoRepository torneoRepo;
    private final CategoriaTorneoRepository categoriaRepo;
    private final ClubRepository clubRepo;
    private final EquipoTorneoRepository equipoRepo;
    private final InscripcionTorneoRepository inscripcionRepo;

    // --------------------------------------------------
    // CREAR TORNEO
    // --------------------------------------------------
    @Transactional
    public Torneo crearTorneo(CrearTorneoDTO dto, Authentication auth) {
        Torneo t = new Torneo();
        t.setNombre(dto.getNombre());
        t.setDescripcion(dto.getDescripcion());

        if (dto.getFechaInicio() != null)
            t.setFechaInicio(Timestamp.valueOf(dto.getFechaInicio()));
        if (dto.getFechaFin() != null)
            t.setFechaFin(Timestamp.valueOf(dto.getFechaFin()));
        if (dto.getFechaAperturaInscripcion() != null)
            t.setFechaAperturaInscripcion(Timestamp.valueOf(dto.getFechaAperturaInscripcion()));
        if (dto.getFechaCierreInscripcion() != null)
            t.setFechaCierreInscripcion(Timestamp.valueOf(dto.getFechaCierreInscripcion()));

        if (dto.getEstado() != null && !dto.getEstado().isEmpty()) {
            t.setEstado(dto.getEstado());
        } else {
            t.setEstado("BORRADOR");
        }

        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            t.setCreadoPor(usuario.getIdUsuario());
        } else {
            throw new RuntimeException("No autenticado");
        }

        return torneoRepo.save(t);
    }

    // --------------------------------------------------
    // LISTAR Y OBTENER
    // --------------------------------------------------
    public Page<Torneo> listar(Pageable pageable, String q) {
        String term = (q == null || q.isBlank()) ? null : q.trim();
        return torneoRepo.buscar(term, pageable);
    }

    public List<Torneo> listarDisponibles() {
        return torneoRepo.findByEstado("INSCRIPCIONES_ABIERTAS");
    }

    public List<Torneo> listarDisponiblesParaClub(String idUsuarioClub) {
        Club club = clubRepo.findByUsuario_IdUsuario(idUsuarioClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        return torneoRepo.findByEstado("INSCRIPCIONES_ABIERTAS").stream()
                .filter(torneo -> !clubTieneInscripcionEnTorneo(club.getIdClub(), torneo.getIdTorneo()))
                .toList();
    }

    public Torneo obtener(String id) {
        return torneoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
    }

    // --------------------------------------------------
    // EDITAR TORNEO
    // --------------------------------------------------
    @Transactional
    public Torneo editar(String id, Torneo datos) {
        Torneo t = obtener(id);
        t.setNombre(datos.getNombre());
        t.setDescripcion(datos.getDescripcion());
        t.setFechaInicio(datos.getFechaInicio());
        t.setFechaFin(datos.getFechaFin());
        t.setFechaAperturaInscripcion(datos.getFechaAperturaInscripcion());
        t.setFechaCierreInscripcion(datos.getFechaCierreInscripcion());

        if (datos.getEstado() != null) {
            t.setEstado(datos.getEstado());
        }
        return torneoRepo.save(t);
    }

    // --------------------------------------------------
    // GESTIÓN DE ESTADOS
    // --------------------------------------------------
    @Transactional
    public Torneo abrirInscripciones(String id) {
        Torneo t = obtener(id);
        Date hoy = new Date();
        if (hoy.before(t.getFechaAperturaInscripcion())) throw new RuntimeException("Aún no es la fecha de apertura");
        if (hoy.after(t.getFechaCierreInscripcion())) throw new RuntimeException("La fecha de inscripción ya expiró");
        t.setEstado("INSCRIPCIONES_ABIERTAS");
        return torneoRepo.save(t);
    }

    @Transactional
    public Torneo cerrarInscripciones(String id) {
        Torneo t = obtener(id);
        t.setEstado("EN_PROGRESO");
        return torneoRepo.save(t);
    }

    @Transactional
    public Torneo cambiarEstado(String id, String nuevoEstado) {
        Torneo t = obtener(id);
        if ("FINALIZADO".equals(t.getEstado())) throw new RuntimeException("No se puede modificar un torneo finalizado");

        List<String> permitidos = List.of("BORRADOR", "INSCRIPCIONES_ABIERTAS", "INSCRIPCIONES_CERRADAS", "EN_PROGRESO", "FINALIZADO");
        if (!permitidos.contains(nuevoEstado)) throw new RuntimeException("Estado inválido: " + nuevoEstado);

        t.setEstado(nuevoEstado);
        return torneoRepo.save(t);
    }

    // --------------------------------------------------
    // ELIMINAR TORNEO (VERSIÓN DEFINITIVA CORREGIDA)
    // --------------------------------------------------
    @Transactional
    public void eliminar(String id) {
        // 1. Buscamos el torneo con sus relaciones
        Torneo torneo = torneoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado para eliminar"));

        // 2. Limpiamos las categorías manualmente.
        // Esto dispara el borrado en cascada (orphanRemoval) de Categorías, Encuentros e Inscripciones.
        if (torneo.getCategorias() != null) {
            torneo.getCategorias().clear();
        }

        // 3. Sincronizamos con la DB para borrar los hijos primero y evitar conflictos con Robots
        torneoRepo.saveAndFlush(torneo);

        // 4. Finalmente borramos el registro padre del torneo
        torneoRepo.delete(torneo);
    }

    // --------------------------------------------------
    // LISTADOS ADICIONALES
    // --------------------------------------------------
    public List<Torneo> listarPublicos() {
        return torneoRepo.findByEstadoIn(List.of("INSCRIPCIONES_ABIERTAS", "EN_PROGRESO", "FINALIZADO"));
    }

    public List<CategoriaTorneo> listarCategorias(String idTorneo) {
        Torneo torneo = obtener(idTorneo);
        return categoriaRepo.findByTorneo(torneo);
    }

    public List<Torneo> listarPorAdministrador(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            return torneoRepo.findAll();
        }
        if (usuario.getRoles().contains(com.robotech.robotech_backend.model.enums.RolUsuario.ADMINISTRADOR)) {
            return torneoRepo.findAll();
        }
        return torneoRepo.findByCreadoPor(usuario.getIdUsuario());
    }

    private boolean clubTieneInscripcionEnTorneo(String idClub, String idTorneo) {
        boolean tieneEquipos = equipoRepo
                .existsByClubIdClubAndCategoriaTorneoTorneoIdTorneoAndEstadoNot(
                        idClub,
                        idTorneo,
                        EstadoEquipoTorneo.ANULADA
                );

        if (tieneEquipos) {
            return true;
        }

        return inscripcionRepo
                .existsByRobotCompetidorClubActualIdClubAndCategoriaTorneoTorneoIdTorneoAndEstadoNot(
                        idClub,
                        idTorneo,
                        EstadoInscripcion.ANULADA
                );
    }
}


