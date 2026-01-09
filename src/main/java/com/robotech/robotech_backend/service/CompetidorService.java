package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CompetidorActualizarDTO;
import com.robotech.robotech_backend.dto.CompetidorClubDTO;
import com.robotech.robotech_backend.dto.CompetidorPerfilDTO;
import com.robotech.robotech_backend.model.Competidor;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import com.robotech.robotech_backend.repository.TorneoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompetidorService {

    private final CompetidorRepository competidorRepo;
    private final RobotRepository robotRepo;
    private final TorneoRepository torneoRepo;

    // =============================
    // CRUD BÁSICO
    // =============================

    public List<Competidor> listar() {
        return competidorRepo.findAll();
    }

    public Optional<Competidor> obtener(String id) {
        return competidorRepo.findById(id);
    }

    public Competidor crear(Competidor competidor) {
        return competidorRepo.save(competidor);
    }

    public void eliminar(String id) {
        competidorRepo.deleteById(id);
    }

    // =============================
    // PERFIL COMPETIDOR (DTO)
    // =============================

    public CompetidorPerfilDTO obtenerPerfil(String idCompetidor) {

        Competidor c = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no existe"));

        Usuario u = c.getUsuario();

        int totalRobots = robotRepo.countByCompetidor_IdCompetidor(idCompetidor);

        return CompetidorPerfilDTO.builder()
                .idCompetidor(c.getIdCompetidor())
                .nombres(c.getUsuario().getNombres())
                .apellidos(c.getUsuario().getApellidos())
                .dni(c.getDni())

                // USUARIO
                .correo(u.getCorreo())
                .telefono(u.getTelefono())

                // CLUB
                .clubNombre(
                        c.getClubActual() != null ? c.getClubActual().getNombre() : "Sin club"
                )

                .estadoValidacion(c.getEstadoValidacion().name())

                // SOLO ROBOTS
                .totalRobots(totalRobots)

                // TEMPORAL
                .totalTorneos(0)
                .puntosRanking(0)

                // FOTO
                .fotoUrl(c.getFotoUrl())

                .build();
    }

    // =============================
    // FOTO DE PERFIL
    // =============================

    public String subirFoto(String idCompetidor, MultipartFile foto) {

        try {
            Competidor c = competidorRepo.findById(idCompetidor)
                    .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

            File carpeta = new File("uploads/competidores");
            if (!carpeta.exists()) carpeta.mkdirs();

            String nombreArchivo = UUID.randomUUID() + "_" + foto.getOriginalFilename();
            Path ruta = Paths.get("uploads/competidores/" + nombreArchivo);

            Files.write(ruta, foto.getBytes());

            c.setFotoUrl("/uploads/competidores/" + nombreArchivo);
            competidorRepo.save(c);

            return c.getFotoUrl();

        } catch (Exception e) {
            throw new RuntimeException("Error al subir la foto");
        }
    }

    public void actualizarTelefono(String idCompetidor, String telefono) {

        Competidor c = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        Usuario u = c.getUsuario();
        u.setTelefono(telefono);
    }

    @Transactional
    public void actualizarPerfil(String idCompetidor, CompetidorActualizarDTO dto) {

        Competidor competidor = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        // Actualizar datos propios
        competidor.getUsuario().setNombres(dto.getNombres());
        competidor.getUsuario().setApellidos(dto.getApellidos());

        // Actualizar datos del usuario
        Usuario usuario = competidor.getUsuario();
        usuario.setTelefono(dto.getTelefono());
    }

    public List<CompetidorClubDTO> listarPorClub(String idClub) {

        return competidorRepo
                .findByClubActual_IdClub(idClub)
                .stream()
                .map(c -> new CompetidorClubDTO(
                        c.getIdCompetidor(),
                        c.getUsuario().getNombres(),
                        c.getUsuario().getApellidos(),
                        c.getDni(),
                        c.getEstadoValidacion().name()
                ))
                .toList();
    }


}
