package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ClubResponseDTO;
import com.robotech.robotech_backend.dto.CrearClubDTO;
import com.robotech.robotech_backend.dto.EditarClubDTO;
import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminClubService {

    private final ClubRepository clubRepo;
    private final UsuarioRepository usuarioRepo;

    public ClubResponseDTO crearClub(CrearClubDTO dto) {

        // 1. Crear usuario propietario
        Usuario propietario = Usuario.builder()
                .correo(dto.getCorreoPropietario())
                .contrasenaHash(dto.getContrasenaPropietario())
                .telefono(dto.getTelefonoPropietario())
                .rol("CLUB")
                .estado("ACTIVO")
                .build();

        usuarioRepo.save(propietario);

        // 2. Crear club usando tu entidad EXACTA
        Club club = Club.builder()
                .codigoClub(generarCodigoClub())   // ← GENERA CÓDIGO CLUB
                .nombre(dto.getNombre())
                .correoContacto(dto.getCorreoContacto())
                .telefonoContacto(dto.getTelefonoContacto())
                .direccionFiscal(dto.getDireccionFiscal())
                .estado("ACTIVO")
                .usuario(propietario)
                .build();

        clubRepo.save(club);

        return mapClub(club);
    }

    private ClubResponseDTO mapClub(Club c) {
        return new ClubResponseDTO(
                c.getIdClub(),
                c.getCodigoClub(),
                c.getNombre(),
                c.getCorreoContacto(),
                c.getTelefonoContacto(),
                c.getDireccionFiscal(),
                c.getEstado(),
                c.getUsuario().getCorreo()
        );
    }

    public List<ClubResponseDTO> listar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return clubRepo.findAll().stream().map(this::mapClub).toList();
        }
        return clubRepo.findByNombreContainingIgnoreCase(nombre)
                .stream().map(this::mapClub).toList();
    }

    // 🔹 Listar con búsqueda opcional
    @GetMapping
    public List<Club> listarClubes(@RequestParam(required = false) String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return clubRepo.findAll();
        }
        return clubRepo.findByNombreContainingIgnoreCase(nombre);
    }

    // 🔹 Obtener club por ID
    @GetMapping("/{id}")
    public Club obtenerClub(@PathVariable String id) {
        return clubRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));
    }

    // 🔹 Editar club
    @PutMapping("/{id}")
    public ClubResponseDTO editar(String idClub, EditarClubDTO dto) {

        Club club = clubRepo.findById(idClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        club.setNombre(dto.getNombre());
        club.setCorreoContacto(dto.getCorreoContacto());
        club.setTelefonoContacto(dto.getTelefonoContacto());
        club.setDireccionFiscal(dto.getDireccionFiscal());
        club.setEstado(dto.getEstado());

        clubRepo.save(club);

        return mapClub(club);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        clubRepo.deleteById(id);
    }

    // ---------------------------------------------------
    // Generador de CODE CLUB (8 caracteres alfanuméricos)
    // ---------------------------------------------------
    private String generarCodigoClub() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
