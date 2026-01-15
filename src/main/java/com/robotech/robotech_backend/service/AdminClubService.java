package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ClubResponseDTO;
import com.robotech.robotech_backend.dto.CrearClubDTO;
import com.robotech.robotech_backend.dto.EditarClubDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.EmailSuggestionService;
import com.robotech.robotech_backend.service.validadores.EmailTakenException;
import com.robotech.robotech_backend.service.validadores.EmailValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminClubService {

    private final ClubRepository clubRepo;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    private final EmailValidator emailValidator;
    private final TelefonoValidator telefonoValidator;
    private final EmailSuggestionService emailSuggestionService;

    // =========================
    // CREAR CLUB
    // =========================
    public ClubResponseDTO crearClub(CrearClubDTO dto) {

        // normalizar correos
        String correoPropietario = normalizarCorreo(dto.getCorreoPropietario());
        String correoContacto   = normalizarCorreo(dto.getCorreoContacto());

        // validar formato
        emailValidator.validar(correoPropietario);
        emailValidator.validar(correoContacto);

        telefonoValidator.validar(dto.getTelefonoPropietario());
        telefonoValidator.validar(dto.getTelefonoContacto());

        // duplicados club
        if (clubRepo.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new RuntimeException("Ya existe un club con ese nombre");
        }

        if (clubRepo.existsByCorreoContacto(correoContacto)) {

            List<String> suggestions =
                    emailSuggestionService.sugerirCorreosHumanosDisponibles(
                            correoContacto,
                            dto.getNombre(), // base: nombre del club
                            "",
                            6
                    );

            throw new EmailTakenException(
                    "correoContacto",
                    "El correo de contacto del club ya está registrado",
                    suggestions
            );
        }

        // duplicados usuario
        if (usuarioRepo.existsByCorreoIgnoreCase(correoPropietario)) {

            List<String> suggestions =
                    emailSuggestionService.sugerirCorreosHumanosDisponibles(
                            correoPropietario,
                            dto.getNombresPropietario(),
                            dto.getApellidosPropietario(),
                            6
                    );

            throw new EmailTakenException(
                    "correoPropietario",
                    "El correo del propietario ya está registrado",
                    suggestions
            );
        }

        if (usuarioRepo.existsByTelefono(dto.getTelefonoPropietario())) {
            throw new RuntimeException("El teléfono del propietario ya está registrado");
        }

        // crear propietario
        Usuario propietario = Usuario.builder()
                .nombres(dto.getNombresPropietario())
                .apellidos(dto.getApellidosPropietario())
                .correo(correoPropietario)
                .contrasenaHash(passwordEncoder.encode(dto.getContrasenaPropietario()))
                .telefono(dto.getTelefonoPropietario())
                .rol(RolUsuario.CLUB)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        usuarioRepo.save(propietario);

        // crear club
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
    // LISTAR
    // =========================
    public List<ClubResponseDTO> listar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return clubRepo.findAll().stream().map(this::mapClub).toList();
        }

        return clubRepo.findByNombreContainingIgnoreCase(nombre)
                .stream().map(this::mapClub).toList();
    }

    // =========================
    // EDITAR
    // =========================
    public ClubResponseDTO editar(String idClub, EditarClubDTO dto) {

        Club club = clubRepo.findById(idClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        if (!club.getNombre().equalsIgnoreCase(dto.getNombre())
                && clubRepo.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new RuntimeException("Ya existe un club con ese nombre");
        }

        String correoContacto = normalizarCorreo(dto.getCorreoContacto());

        emailValidator.validar(correoContacto);
        telefonoValidator.validar(dto.getTelefonoContacto());

        if (!club.getCorreoContacto().equalsIgnoreCase(correoContacto)
                && clubRepo.existsByCorreoContacto(correoContacto)) {
            throw new RuntimeException("El correo de contacto del club ya está registrado");
        }

        club.setNombre(dto.getNombre());
        club.setCorreoContacto(correoContacto);
        club.setTelefonoContacto(dto.getTelefonoContacto());
        club.setDireccionFiscal(dto.getDireccionFiscal());
        club.setEstado(dto.getEstado());

        clubRepo.save(club);

        return mapClub(club);
    }

    // =========================
    // ELIMINAR
    // =========================
    public void eliminar(String idClub) {
        clubRepo.deleteById(idClub);
    }

    // =========================
    // MAPPERS / HELPERS
    // =========================
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

    private String generarCodigoClub() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String normalizarCorreo(String correo) {
        if (correo == null) return null;
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
