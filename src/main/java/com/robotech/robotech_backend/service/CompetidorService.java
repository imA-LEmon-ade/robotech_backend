package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CompetidorActualizarDTO;
import com.robotech.robotech_backend.dto.CompetidorClubDTO;
import com.robotech.robotech_backend.dto.CompetidorPerfilDTO;
import com.robotech.robotech_backend.model.Competidor;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.model.EstadoValidacion; // Importante
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
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
import java.util.stream.Collectors; // Necesario para el filtrado

@Service
@RequiredArgsConstructor
public class CompetidorService {

    private final CompetidorRepository competidorRepo;
    private final RobotRepository robotRepo;
    private final UsuarioRepository usuarioRepo;
    private final DniValidator dniValidator;
    private final TelefonoValidator telefonoValidator;

    // =============================
    // CRUD BÁSICO (INTACTO)
    // =============================
    public List<Competidor> listar() { return competidorRepo.findAll(); }
    public Optional<Competidor> obtener(String id) { return competidorRepo.findById(id); }
    public Competidor crear(Competidor competidor) { return competidorRepo.save(competidor); }
    public void eliminar(String id) { competidorRepo.deleteById(id); }

    // =============================
    // OBTENER PERFIL (INTACTO)
    // =============================
    public CompetidorPerfilDTO obtenerPerfil(String idCompetidor) {
        Competidor c = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no existe"));

        Usuario u = c.getUsuario();
        int totalRobots = robotRepo.countByCompetidor_IdCompetidor(idCompetidor);

        return CompetidorPerfilDTO.builder()
                .idCompetidor(c.getIdCompetidor())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .dni(c.getUsuario().getDni())
                .correo(u.getCorreo())
                .telefono(u.getTelefono())
                .clubNombre(c.getClubActual() != null ? c.getClubActual().getNombre() : "Sin club")
                .estadoValidacion(c.getEstadoValidacion().name())
                .totalRobots(totalRobots)
                .totalTorneos(0)
                .puntosRanking(0)
                .fotoUrl(c.getFotoUrl())
                .build();
    }

    // =============================
    // SUBIR FOTO (INTACTO)
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

    // =============================
    // ACTUALIZAR PERFIL (INTACTO - TU LÓGICA ORIGINAL)
    // =============================
    @Transactional
    public void actualizarPerfil(String idCompetidor, CompetidorActualizarDTO dto) {

        System.out.println("--- ACTUALIZANDO PERFIL ---");
        System.out.println("DNI recibido: " + dto.getDni());

        Competidor competidor = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        Usuario usuario = competidor.getUsuario();

        if (dto.getDni() != null) {
            dniValidator.validar(dto.getDni());
        }
        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            telefonoValidator.validar(dto.getTelefono());
        }

        // 1. Actualizar Usuario
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setTelefono(dto.getTelefono());

        // 2. Actualizar DNI
        if (dto.getDni() != null) {
            competidor.getUsuario().setDni(dto.getDni());
        }

        // 3. Actualizar Correo
        if (dto.getCorreo() != null && !dto.getCorreo().trim().isEmpty()) {
            if (!dto.getCorreo().equals(usuario.getCorreo())) {
                if (usuarioRepo.existsByCorreo(dto.getCorreo())) {
                    throw new RuntimeException("El correo ya está en uso");
                }
                usuario.setCorreo(dto.getCorreo());
            }
        }

        competidorRepo.save(competidor);
    }

    // =======================================================
    //  NUEVOS MÉTODOS Y SOBRECARGAS (CON CORRECCIÓN DE BÚSQUEDA)
    // =======================================================

    // 1.A. LISTAR POR CLUB (COMPATIBILIDAD)
    public List<CompetidorClubDTO> listarPorClub(String idClub) {
        return listarPorClub(idClub, null); // Llama al nuevo con búsqueda null
    }

    // 1.B. LISTAR POR CLUB (CORREGIDO: FILTRADO EN JAVA)
    public List<CompetidorClubDTO> listarPorClub(String idClub, String busqueda) {

        // 1. Obtenemos TODOS los competidores de ese club (Consulta simple y segura)
        // NOTA: Asegúrate que tu repositorio tenga 'findByClubActual_IdClub' o 'findByClub_IdClub'
        List<Competidor> listaCompleta = competidorRepo.findByClubActual_IdClub(idClub);

        // 2. Filtramos en memoria (Java Stream) para evitar errores de JPQL
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            String termino = busqueda.toLowerCase().trim();

            listaCompleta = listaCompleta.stream()
                    .filter(c -> {
                        // Null-safe checks
                        String nombres = c.getUsuario().getNombres() != null ? c.getUsuario().getNombres().toLowerCase() : "";
                        String apellidos = c.getUsuario().getApellidos() != null ? c.getUsuario().getApellidos().toLowerCase() : "";
                        String dni = c.getUsuario().getDni() != null ? c.getUsuario().getDni() : "";
                        String correo = c.getUsuario().getCorreo() != null ? c.getUsuario().getCorreo().toLowerCase() : "";

                        // Búsqueda flexible
                        return nombres.contains(termino) ||
                                apellidos.contains(termino) ||
                                dni.contains(termino) ||
                                correo.contains(termino);
                    })
                    .collect(Collectors.toList());
        }

        // 3. Mapeo a DTO
        return listaCompleta.stream()
                .map(c -> new CompetidorClubDTO(
                        c.getIdCompetidor(),
                        c.getUsuario().getNombres(),
                        c.getUsuario().getApellidos(),
                        c.getUsuario().getDni(),
                        c.getEstadoValidacion().name(),
                        c.getUsuario().getCorreo()
                ))
                .toList();
    }

    // 2. APROBAR COMPETIDOR (INTACTO)
    @Transactional
    public void aprobarCompetidor(String idCompetidor) {
        Competidor competidor = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        if (!competidor.getEstadoValidacion().equals(EstadoValidacion.PENDIENTE)) {
            throw new RuntimeException("El competidor ya fue procesado (Estado: " + competidor.getEstadoValidacion() + ")");
        }

        competidor.setEstadoValidacion(EstadoValidacion.APROBADO);
        competidorRepo.save(competidor);
    }

    // 3. RECHAZAR COMPETIDOR (INTACTO)
    @Transactional
    public void rechazarCompetidor(String idCompetidor) {
        Competidor competidor = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        competidor.setEstadoValidacion(EstadoValidacion.RECHAZADO);
        competidorRepo.save(competidor);
    }
}
