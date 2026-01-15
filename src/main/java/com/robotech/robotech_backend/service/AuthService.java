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

    // -------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------
    public LoginResponseDTO login(String correo, String contrasena) {

        Usuario usuario = usuarioRepo.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(contrasena, usuario.getContrasenaHash())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new RuntimeException("Cuenta inactiva");
        }

        String token = jwtService.generarToken(usuario);

        Object entidad;

        switch (usuario.getRol()) {

            case CLUB -> {
                Club club = clubRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                        .orElseThrow(() -> new RuntimeException("Club no encontrado"));

                entidad = new ClubLoginDTO(
                        club.getIdClub(),
                        club.getNombre(),
                        club.getCorreoContacto(),
                        club.getTelefonoContacto()
                );
            }

            case COMPETIDOR -> {
                Competidor c = competidorRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                        .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

                entidad = new CompetidorLoginDTO(
                        c.getIdCompetidor(),
                        usuario.getNombres(),
                        usuario.getApellidos(),
                        usuario.getCorreo(),
                        c.getClubActual().getIdClub(),
                        c.getClubActual().getNombre()
                );
            }

            case JUEZ -> {
                Juez j = juezRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                        .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

                entidad = Map.of(
                        "idJuez", j.getIdJuez(),
                        "correo", usuario.getCorreo()
                );
            }

            case ADMINISTRADOR, SUBADMINISTRADOR -> {
                entidad = usuario;
            }

            default -> throw new RuntimeException("Rol no soportado");
        }

        return new LoginResponseDTO(
                token,
                usuario.getRol(),
                entidad
        );
    }


    // -------------------------------------------------------
    // REGISTRO COMPETIDOR
    // -------------------------------------------------------
    @Transactional
    public void registrarCompetidor(RegistroCompetidorDTO dto) {

        if (usuarioRepo.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Correo ya registrado");
        }

        if (usuarioRepo.existsByTelefono(dto.getTelefono())) {
            throw new RuntimeException("Teléfono ya registrado");
        }

        CodigoRegistroCompetidor codigo =
                codigoService.validarCodigo(dto.getCodigoClub());

        Usuario usuario = Usuario.builder()
                .nombres(dto.getNombre())
                .apellidos(dto.getApellido())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .rol(RolUsuario.COMPETIDOR)
                .estado(EstadoUsuario.PENDIENTE)
                .build();

        usuarioRepo.save(usuario);

        Competidor competidor = Competidor.builder()
                .dni(dto.getDni())
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

        if (usuarioRepo.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Correo ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .rol(RolUsuario.CLUB)
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

        if (usuarioRepo.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Correo ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .rol(RolUsuario.JUEZ)
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
