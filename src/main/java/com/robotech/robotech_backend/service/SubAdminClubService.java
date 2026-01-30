package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ClubResponseDTO;
import com.robotech.robotech_backend.dto.CrearClubDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.EmailSuggestionService;
import com.robotech.robotech_backend.service.validadores.EmailValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class SubAdminClubService {

    private final ClubRepository clubRepo;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    private final EmailValidator emailValidator;
    private final TelefonoValidator telefonoValidator;
    private final EmailSuggestionService emailSuggestionService;

    // =========================
    // CREAR CLUB (SUBADMIN)
    // =========================
    public ClubResponseDTO crearClub(CrearClubDTO dto) {

        // Normalizar correos
        String correoPropietario = normalizarCorreo(dto.getCorreoPropietario());
        String correoContacto   = normalizarCorreo(dto.getCorreoContacto());

        // Validar formatos
        emailValidator.validar(correoPropietario);
        emailValidator.validar(correoContacto);

        telefonoValidator.validar(dto.getTelefonoPropietario());
        telefonoValidator.validar(dto.getTelefonoContacto());

        // Duplicados CLUB
        if (clubRepo.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new RuntimeException("Ya existe un club con ese nombre");
        }

        if (clubRepo.existsByCorreoContacto(correoContacto)) {
            throw new RuntimeException("El correo de contacto del club ya está registrado");
        }

        if (clubRepo.existsByTelefonoContacto(dto.getTelefonoContacto())) {
            throw new RuntimeException("El teléfono de contacto del club ya está registrado");
        }

        // Duplicados PROPIETARIO
        if (usuarioRepo.existsByCorreoIgnoreCase(correoPropietario)) {
            throw new RuntimeException("El correo del propietario ya está registrado");
        }

        if (usuarioRepo.existsByTelefono(dto.getTelefonoPropietario())) {
            throw new RuntimeException("El teléfono del propietario ya está registrado");
        }

        if (usuarioRepo.existsByDni(dto.getDniPropietario())) {
            throw new RuntimeException("El DNI del propietario ya está registrado");
        }

        // Crear USUARIO propietario
        Usuario propietario = Usuario.builder()
                .dni(dto.getDniPropietario())
                .nombres(dto.getNombresPropietario())
                .apellidos(dto.getApellidosPropietario())
                .correo(correoPropietario)
                .telefono(dto.getTelefonoPropietario())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasenaPropietario()))
                .rol(RolUsuario.CLUB)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        usuarioRepo.save(propietario);

        // Crear CLUB
        Club club = Club.builder()
                .codigoClub(generarCodigoClub())
                .nombre(dto.getNombre())
                .correoContacto(correoContacto)
                .telefonoContacto(dto.getTelefonoContacto())
                .direccionFiscal(dto.getDireccionFiscal())
                .estado(EstadoClub.ACTIVO)
                .usuario(propietario)
                .build();

        clubRepo.save(club);

        return mapClub(club);
    }

    // =========================
    // HELPERS
    // =========================
    private String normalizarCorreo(String correo) {
        if (correo == null) return null;
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private String generarCodigoClub() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
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
                c.getUsuario().getCorreo(),
                c.getUsuario().getDni(),        // 👈 Mapear DNI
                c.getUsuario().getNombres(),    // 👈 Mapear Nombres
                c.getUsuario().getApellidos(),
                c.getUsuario().getTelefono()// 👈 Mapear Apellidos
        );
    }
}
