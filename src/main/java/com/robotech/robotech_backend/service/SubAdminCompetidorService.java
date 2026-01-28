package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CompetidorResponseDTO;
import com.robotech.robotech_backend.dto.RegistroCompetidorDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubAdminCompetidorService {

    private final UsuarioRepository usuarioRepo;
    private final CompetidorRepository competidorRepo;
    private final ClubRepository clubRepo; // Necesario para validar el club
    private final PasswordEncoder passwordEncoder;

    public List<CompetidorResponseDTO> listarTodos() {
        return competidorRepo.findAll().stream().map(comp -> {
            Usuario u = comp.getUsuario();
            return new CompetidorResponseDTO(
                    u.getIdUsuario(),
                    u.getNombres(),
                    u.getApellidos(),
                    u.getDni(),
                    u.getCorreo(),
                    comp.getClubActual().getNombre(),
                    comp.getEstadoValidacion().name(),
                    u.getTelefono()
            );
        }).toList();
    }

    public void registrarCompetidor(RegistroCompetidorDTO dto) {

        // 1️⃣ Validar si el correo ya existe
        if (usuarioRepo.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // 2️⃣ Buscar el club por el código que viene del DTO
        Club club = clubRepo.findById(dto.getCodigoClub())
                .orElseThrow(() -> new RuntimeException("El club con código " + dto.getCodigoClub() + " no existe"));

        // 3️⃣ Crear el usuario base (Mapeando nombre -> nombres y apellido -> apellidos)
        Usuario usuario = Usuario.builder()
                .dni(dto.getDni())
                .nombres(dto.getNombre())    // Mapeo manual del DTO
                .apellidos(dto.getApellido()) // Mapeo manual del DTO
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .rol(RolUsuario.COMPETIDOR)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        usuarioRepo.save(usuario);

        // 4️⃣ Crear el competidor vinculado (MapsId)
        Competidor competidor = Competidor.builder()
                .usuario(usuario) // Vinculación para @MapsId
                .clubActual(club)
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .build();

        competidorRepo.save(competidor);
    }
}