package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.*;
import com.robotech.robotech_backend.model.*;

import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.JuezRepository;

import com.robotech.robotech_backend.security.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final ClubRepository clubRepo;
    private final CompetidorRepository competidorRepo;
    private final JuezRepository juezRepo;
    private final CodigoRegistroService codigoService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.robotech.robotech_backend.service.validadores.DniValidator dniValidator;
    private final com.robotech.robotech_backend.service.validadores.TelefonoValidator telefonoValidator;

    // -------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------
    public LoginResponseDTO login(String correo, String contrasena) {

        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            throw new RuntimeException("Por favor rellena los campos");
        }

        Usuario usuario = usuarioRepo.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(contrasena, usuario.getContrasenaHash())) {
            throw new RuntimeException("Contrasena incorrecta");
        }

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new RuntimeException("Cuenta inactiva");
        }

        String token = jwtService.generarToken(usuario);

        Set<RolUsuario> roles = usuario.getRoles() != null ? usuario.getRoles() : java.util.Set.of();
        Map<String, Object> entidad = new HashMap<>();

        if (roles.contains(RolUsuario.CLUB)) {
            Club club = clubRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Club no encontrado"));

            if (club.getEstado() != EstadoClub.ACTIVO) {
                throw new RuntimeException("Club inactivo");
            }

            entidad.put("club", new ClubLoginDTO(
                    club.getIdClub(),
                    club.getNombre(),
                    club.getCorreoContacto(),
                    club.getTelefonoContacto()
            ));
        }

        if (roles.contains(RolUsuario.COMPETIDOR)) {
            Competidor c = competidorRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

            if (c.getEstadoValidacion() != EstadoValidacion.APROBADO) {
                throw new RuntimeException("Competidor no aprobado");
            }

            boolean clubValido = c.getClubActual() != null && c.getClubActual().getEstado() == EstadoClub.ACTIVO;
            boolean libre = c.getClubActual() == null;
            if (!clubValido && !roles.contains(RolUsuario.JUEZ) && !libre) {
                throw new RuntimeException("Club inactivo");
            }

            entidad.put("competidor", new CompetidorLoginDTO(
                    c.getIdCompetidor(),
                    usuario.getNombres(),
                    usuario.getApellidos(),
                    usuario.getCorreo(),
                    c.getClubActual() != null ? c.getClubActual().getIdClub() : null,
                    c.getClubActual() != null ? c.getClubActual().getNombre() : "Agente libre"
            ));
        }

        if (roles.contains(RolUsuario.JUEZ)) {
            Juez j = juezRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

            entidad.put("juez", Map.of(
                    "idJuez", j.getIdJuez(),
                    "correo", usuario.getCorreo()
            ));
        }

        if (roles.contains(RolUsuario.ADMINISTRADOR) || roles.contains(RolUsuario.SUBADMINISTRADOR)) {
            entidad.put("usuario", usuario);
        }

        if (entidad.isEmpty()) {
            entidad.put("usuario", usuario);
        }

        return new LoginResponseDTO(
                token,
                roles,
                entidad
        );
    }


    // -------------------------------------------------------
    // REGISTRO COMPETIDOR
    // -------------------------------------------------------
    @Transactional
    public void registrarCompetidor(RegistroCompetidorDTO dto) {

        dniValidator.validar(dto.getDni());
        telefonoValidator.validar(dto.getTelefono());

        if (usuarioRepo.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Correo ya registrado");
        }

        if (usuarioRepo.existsByTelefono(dto.getTelefono())) {
            throw new RuntimeException("Tel?fono ya registrado");
        }

        CodigoRegistroCompetidor codigo =
                codigoService.validarCodigo(dto.getCodigoClub());

        Usuario usuario = Usuario.builder()
                .dni(dto.getDni())
                .nombres(dto.getNombre())
                .apellidos(dto.getApellido())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .roles(Set.of(RolUsuario.COMPETIDOR))
                .estado(EstadoUsuario.PENDIENTE)
                .build();

        usuarioRepo.save(usuario);

        Competidor competidor = Competidor.builder()
                .usuario(usuario)
                .clubActual(codigo.getClub())
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .build();

        competidorRepo.save(competidor);

        codigoService.marcarUso(codigo);
    }

    // -------------------------------------------------------
    // REGISTRO CLUB
    // -------------------------------------------------------
    @Transactional
    public void registrarClub(RegistroClubDTO dto) {

        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            telefonoValidator.validar(dto.getTelefono());
        }

        if (usuarioRepo.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Correo ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .roles(Set.of(RolUsuario.CLUB))
                .estado(EstadoUsuario.PENDIENTE)
                .build();

        usuarioRepo.save(usuario);

        String codigoClub = "CLB-" +
                usuario.getIdUsuario().substring(0, 6).toUpperCase();

        Club club = Club.builder()
                .nombre(dto.getNombre())
                .correoContacto(dto.getCorreo())
                .telefonoContacto(dto.getTelefono())
                .direccionFiscal(dto.getDireccionFiscal())
                .estado(EstadoClub.PENDIENTE)
                .codigoClub(codigoClub)
                .usuario(usuario)
                .build();

        clubRepo.save(club);
    }

    // -------------------------------------------------------
    // REGISTRO JUEZ
    // -------------------------------------------------------
    @Transactional
    public void registrarJuez(RegistroJuezDTO dto) {

        dniValidator.validar(dto.getDni());
        telefonoValidator.validar(dto.getTelefono());

        if (usuarioRepo.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Correo ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .roles(Set.of(RolUsuario.JUEZ))
                .estado(EstadoUsuario.PENDIENTE)
                .build();

        usuarioRepo.save(usuario);

        Juez juez = Juez.builder()
                .usuario(usuario)
                .licencia(dto.getLicencia())
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .build();

        juezRepo.save(juez);
    }
}
