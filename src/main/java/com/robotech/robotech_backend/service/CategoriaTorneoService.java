package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CategoriaTorneoDTO;
import com.robotech.robotech_backend.dto.CategoriaTorneoPublicoDTO;
import com.robotech.robotech_backend.model.ModalidadCategoria;
import com.robotech.robotech_backend.model.Torneo;
import com.robotech.robotech_backend.model.CategoriaTorneo;
import com.robotech.robotech_backend.model.EstadoInscripcion;
import com.robotech.robotech_backend.model.EquipoTorneo;
import com.robotech.robotech_backend.model.EstadoEquipoTorneo;
import com.robotech.robotech_backend.model.InscripcionTorneo;
import com.robotech.robotech_backend.repository.TorneoRepository;
import com.robotech.robotech_backend.repository.HistorialCalificacionRepository;
import com.robotech.robotech_backend.repository.InscripcionTorneoRepository;
import com.robotech.robotech_backend.repository.EquipoTorneoRepository;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaTorneoService {

    private final CategoriaTorneoRepository repo;
    private final TorneoRepository torneoRepo;
    private final HistorialCalificacionRepository historialRepo;
    private final InscripcionTorneoRepository inscripcionRepo;
    private final EquipoTorneoRepository equipoRepo;

    public List<CategoriaTorneo> listarPorTorneo(String idTorneo) {
        return repo.findByTorneoIdTorneo(idTorneo);
    }

    public List<CategoriaTorneoPublicoDTO> listarPublicoPorTorneo(String idTorneo) {
        Date hoy = new Date();
        return repo.findByTorneoIdTorneo(idTorneo).stream()
                .map(c -> {
                    boolean cierrePorFecha = c.getTorneo() != null
                            && c.getTorneo().getFechaCierreInscripcion() != null
                            && hoy.after(c.getTorneo().getFechaCierreInscripcion());
                    boolean inscripcionesCerradas = Boolean.TRUE.equals(c.getInscripcionesCerradas()) || cierrePorFecha;

                    return CategoriaTorneoPublicoDTO.builder()
                        .idCategoriaTorneo(c.getIdCategoriaTorneo())
                        .categoria(c.getCategoria() != null ? c.getCategoria().name() : null)
                        .modalidad(c.getModalidad())
                        .descripcion(c.getDescripcion())
                        .maxParticipantes(c.getMaxParticipantes())
                        .maxEquipos(c.getMaxEquipos())
                        .maxIntegrantesEquipo(c.getMaxIntegrantesEquipo())
                        .inscripcionesCerradas(inscripcionesCerradas)
                        .build();
                })
                .toList();
    }

    public CategoriaTorneo crear(String idTorneo, CategoriaTorneoDTO dto) {

        Torneo torneo = torneoRepo.findById(idTorneo)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        if (dto.getModalidad() == ModalidadCategoria.INDIVIDUAL) {

            if (dto.getMaxParticipantes() == null) {
                throw new IllegalArgumentException(
                        "maxParticipantes es obligatorio para categorías individuales"
                );
            }

            dto.setMaxEquipos(null);
            dto.setMaxIntegrantesEquipo(null);

        } else { // EQUIPO

            if (dto.getMaxEquipos() == null || dto.getMaxIntegrantesEquipo() == null) {
                throw new IllegalArgumentException(
                        "maxEquipos y maxIntegrantesEquipo son obligatorios para categorías por equipo"
                );
            }

            dto.setMaxParticipantes(null);
        }

        CategoriaTorneo categoria = CategoriaTorneo.builder()
                .torneo(torneo)
                .categoria(dto.getCategoria())
                .modalidad(dto.getModalidad())
                .maxParticipantes(dto.getMaxParticipantes())
                .maxEquipos(dto.getMaxEquipos())
                .maxIntegrantesEquipo(dto.getMaxIntegrantesEquipo())
                .descripcion(dto.getDescripcion())
                .inscripcionesCerradas(false)
                .build();

        return repo.save(categoria);
    }

    public CategoriaTorneo editar(String idCategoria, CategoriaTorneoDTO dto) {

        CategoriaTorneo c = repo.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        c.setCategoria(dto.getCategoria());
        c.setMaxParticipantes(dto.getMaxParticipantes());
        c.setDescripcion(dto.getDescripcion());

        return repo.save(c);
    }

    public String eliminar(String idCategoria) {
        CategoriaTorneo categoria = repo.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        boolean tieneCalificaciones = !historialRepo
                .findByEncuentro_CategoriaTorneo_IdCategoriaTorneo(idCategoria)
                .isEmpty();
        if (tieneCalificaciones) {
            throw new RuntimeException("No se puede eliminar: la categoría ya tiene calificaciones registradas");
        }

        int inscripcionesAnuladas = 0;
        int equiposAnulados = 0;

        if (categoria.getModalidad() == ModalidadCategoria.INDIVIDUAL) {
            List<InscripcionTorneo> inscripciones = inscripcionRepo.findByCategoriaTorneoIdCategoriaTorneo(idCategoria);
            Date ahora = new Date();
            for (InscripcionTorneo ins : inscripciones) {
                if (ins.getEstado() != EstadoInscripcion.ANULADA) {
                    ins.setEstado(EstadoInscripcion.ANULADA);
                    ins.setMotivoAnulacion("Categoría eliminada");
                    ins.setAnuladaEn(ahora);
                    ins.setAnuladaPor("ADMIN");
                    inscripcionesAnuladas++;
                }
            }
            inscripcionRepo.saveAll(inscripciones);
        } else {
            List<EquipoTorneo> equipos = equipoRepo.findByCategoriaTorneoIdCategoriaTorneo(idCategoria);
            Date ahora = new Date();
            for (EquipoTorneo eq : equipos) {
                if (eq.getEstado() != EstadoEquipoTorneo.ANULADA) {
                    eq.setEstado(EstadoEquipoTorneo.ANULADA);
                    eq.setMotivoAnulacion("Categoría eliminada");
                    eq.setAnuladaEn(ahora);
                    equiposAnulados++;
                }
            }
            equipoRepo.saveAll(equipos);
        }

        repo.deleteById(idCategoria);

        if (inscripcionesAnuladas > 0 || equiposAnulados > 0) {
            return "Categoría eliminada. Inscripciones anuladas: " + inscripcionesAnuladas +
                    ". Equipos anulados: " + equiposAnulados +
                    ". Notifica a los competidores y clubes inscritos.";
        }

        return "Categoría eliminada correctamente";
    }
}
