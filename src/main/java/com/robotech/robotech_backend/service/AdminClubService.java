package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ClubResponseDTO;
import com.robotech.robotech_backend.dto.CrearClubDTO;
import com.robotech.robotech_backend.dto.EditarClubDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.EmailSuggestionService;
import com.robotech.robotech_backend.service.validadores.FieldValidationException;
import com.robotech.robotech_backend.service.validadores.EmailValidator;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminClubService {

    private final ClubRepository clubRepo;
    private final UsuarioRepository usuarioRepo;
    private final CompetidorRepository competidorRepo;
    private final PasswordEncoder passwordEncoder;

    private final EmailValidator emailValidator;
    private final TelefonoValidator telefonoValidator;
    private final DniValidator dniValidator;
    private final EmailSuggestionService emailSuggestionService;

    // =========================
    // CREAR CLUB
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
        dniValidator.validar(dto.getDniPropietario());

        // Validar duplicados CLUB
        if (clubRepo.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new RuntimeException("Ya existe un club con ese nombre");
        }

        if (clubRepo.existsByCorreoContacto(correoContacto)) {
            List<String> suggestions =
                    emailSuggestionService.sugerirCorreosHumanosDisponibles(
                            correoContacto,
                            dto.getNombre(),
                            "",
                            6
                    );

            throw new FieldValidationException(
                    "correoContacto",
                    "El correo de contacto del club ya está registrado",
                    suggestions
            );
        }

        if (clubRepo.existsByTelefonoContacto(dto.getTelefonoContacto())) {
            throw new RuntimeException("El teléfono de contacto del club ya está registrado");
        }

        // Validar duplicados USUARIO (propietario)
        if (usuarioRepo.existsByCorreoIgnoreCase(correoPropietario)) {
            List<String> suggestions =
                    emailSuggestionService.sugerirCorreosHumanosDisponibles(
                            correoPropietario,
                            dto.getNombresPropietario(),
                            dto.getApellidosPropietario(),
                            6
                    );

            throw new FieldValidationException(
                    "correoPropietario",
                    "El correo del propietario ya está registrado",
                    suggestions
            );
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
                .contrasenaHash(passwordEncoder.encode(dto.getContrasenaPropietario()))
                .telefono(dto.getTelefonoPropietario())
                .roles(Set.of(RolUsuario.CLUB, RolUsuario.COMPETIDOR))
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

        // Crear perfil competidor para el propietario
        Competidor competidor = Competidor.builder()
                .usuario(propietario)
                .clubActual(club)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();
        competidorRepo.save(competidor);

        return mapClub(club);
    }

    // =========================
    // LISTAR CLUBES
    // =========================
    public Page<ClubResponseDTO> listar(String nombre, Pageable pageable) {
        String q = (nombre == null || nombre.isBlank()) ? null : nombre.trim();
        return clubRepo.buscarPorNombre(q, pageable)
                .map(this::mapClub);
    }

    // =========================
    // EDITAR CLUB
    // =========================
    public ClubResponseDTO editar(String idClub, EditarClubDTO dto) {

        Club club = clubRepo.findById(idClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        EstadoClub estadoAnterior = club.getEstado();

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

        if (!club.getTelefonoContacto().equals(dto.getTelefonoContacto())
                && clubRepo.existsByTelefonoContacto(dto.getTelefonoContacto())) {
            throw new RuntimeException("El teléfono de contacto del club ya está registrado");
        }

        club.setNombre(dto.getNombre());
        club.setCorreoContacto(correoContacto);
        club.setTelefonoContacto(dto.getTelefonoContacto());
        club.setDireccionFiscal(dto.getDireccionFiscal());
        if (dto.getEstado() != null) {
            club.setEstado(dto.getEstado());
        }

        clubRepo.save(club);

        if (dto.getEstado() != null && dto.getEstado() != estadoAnterior) {
            if (dto.getEstado() == EstadoClub.INACTIVO) {
                Usuario owner = club.getUsuario();
                owner.setEstado(EstadoUsuario.INACTIVO);
                usuarioRepo.save(owner);

                List<Competidor> competidores = competidorRepo.findByClubActual_IdClub(club.getIdClub());
                for (Competidor c : competidores) {
                    Usuario u = c.getUsuario();
                    u.setEstado(EstadoUsuario.INACTIVO);
                    usuarioRepo.save(u);
                }
            } else if (dto.getEstado() == EstadoClub.ACTIVO) {
                Usuario owner = club.getUsuario();
                owner.setEstado(EstadoUsuario.ACTIVO);
                usuarioRepo.save(owner);

                List<Competidor> competidores = competidorRepo.findByClubActual_IdClub(club.getIdClub());
                for (Competidor c : competidores) {
                    if (c.getEstadoValidacion() == EstadoValidacion.APROBADO) {
                        Usuario u = c.getUsuario();
                        u.setEstado(EstadoUsuario.ACTIVO);
                        usuarioRepo.save(u);
                    }
                }
            }
        }

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
                c.getUsuario().getCorreo(),
                c.getUsuario().getDni(),        // 👈 Mapear DNI
                c.getUsuario().getNombres(),    // 👈 Mapear Nombres
                c.getUsuario().getApellidos(),
                c.getUsuario().getTelefono()// 👈 Mapear Apellidos
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


