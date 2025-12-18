package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.Torneo;
import com.robotech.robotech_backend.model.CategoriaTorneo;
import com.robotech.robotech_backend.repository.TorneoRepository;
import com.robotech.robotech_backend.repository.CategoriaTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TorneoService {

    private final TorneoRepository torneoRepo;
    private final CategoriaTorneoRepository categoriaRepo;

    // Crear torneo
    public Torneo crearTorneo(Torneo t) {
        t.setEstado("BORRADOR");
        return torneoRepo.save(t);
    }

    // Listar torneos
    public List<Torneo> listar() {
        return torneoRepo.findAll();
    }

    // Obtener torneo
    public Torneo obtener(String id) {
        return torneoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
    }

    // Editar torneo
    public Torneo editar(String id, Torneo datos) {
        Torneo t = obtener(id);

        t.setNombre(datos.getNombre());
        t.setDescripcion(datos.getDescripcion());
        t.setFechaInicio(datos.getFechaInicio());
        t.setFechaFin(datos.getFechaFin());
        t.setFechaAperturaInscripcion(datos.getFechaAperturaInscripcion());
        t.setFechaCierreInscripcion(datos.getFechaCierreInscripcion());
        t.setTipo(datos.getTipo());

        return torneoRepo.save(t);
    }

    // Abrir inscripciones
    public Torneo abrirInscripciones(String id) {
        Torneo t = obtener(id);

        Date hoy = new Date();
        if (hoy.before(t.getFechaAperturaInscripcion())) {
            throw new RuntimeException("Aún no es la fecha de apertura.");
        }
        if (hoy.after(t.getFechaCierreInscripcion())) {
            throw new RuntimeException("Ya pasó la fecha máxima de inscripción.");
        }

        t.setEstado("INSCRIPCIONES_ABIERTAS");
        return torneoRepo.save(t);
    }

    // Cerrar inscripciones
    public Torneo cerrarInscripciones(String id) {
        Torneo t = obtener(id);

        t.setEstado("EN_PROGRESO"); // o "CERRADO", si deseas crear ese estado
        return torneoRepo.save(t);
    }

    // Cambiar estado del torneo manualmente
    public Torneo cambiarEstado(String id, String nuevoEstado) {
        Torneo t = obtener(id);

        // 🔒 VALIDACIÓN CLAVE
        if ("FINALIZADO".equals(t.getEstado())) {
            throw new RuntimeException("No se puede modificar un torneo finalizado");
        }

        List<String> permitidos = List.of(
                "BORRADOR",
                "INSCRIPCIONES_ABIERTAS",
                "EN_PROGRESO",
                "FINALIZADO"
        );

        if (!permitidos.contains(nuevoEstado)) {
            throw new RuntimeException("Estado inválido");
        }

        t.setEstado(nuevoEstado);
        return torneoRepo.save(t);
    }


    // Eliminar torneo
    public void eliminar(String id) {
        torneoRepo.deleteById(id);
    }

    public List<Torneo> listarParaClub() {
        return torneoRepo.findByEstado("INSCRIPCIONES_ABIERTAS");
    }

    public List<Torneo> listarPublicos() {
        return torneoRepo.findByEstadoIn(
                List.of("INSCRIPCIONES_ABIERTAS", "EN_PROGRESO", "FINALIZADO")
        );
    }

    public List<Torneo> listarParaCompetidor() {
        return torneoRepo.findByEstadoIn(
                List.of("INSCRIPCIONES_ABIERTAS", "EN_PROGRESO")
        );
    }

    public List<CategoriaTorneo> listarCategorias(String idTorneo) {
        Torneo torneo = obtener(idTorneo);
        return categoriaRepo.findByTorneo(torneo);
    }
    public List<Torneo> listarParaClub(Authentication auth) {
        // por ahora solo filtramos por estado
        return torneoRepo.findByEstado("INSCRIPCIONES_ABIERTAS");}



}
