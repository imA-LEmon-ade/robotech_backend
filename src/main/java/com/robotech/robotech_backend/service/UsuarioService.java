package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CodigoRegistroCompetidorRepository; // ✅ Importante
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.model.EstadoUsuario;
import com.robotech.robotech_backend.model.RolUsuario;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CompetidorRepository competidorRepository;
    private final CodigoRegistroCompetidorRepository codigoRepo; // ✅ Repositorio de códigos
    private final PasswordEncoder passwordEncoder;
    private final NicknameValidator nicknameValidator;

    // (Opcional: Si necesitas el ClubRepo para otras cosas, déjalo, si no, puedes quitarlo)
    // private final ClubRepository clubRepository;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Transactional
    public Usuario crearUsuario(CrearUsuarioDTO dto, String dni, String codigoIngresado) {
        // --- 1. VALIDACIONES BÁSICAS ---
        nicknameValidator.validar(dto.nombres());
        nicknameValidator.validar(dto.apellidos());

        if (usuarioRepository.existsByCorreo(dto.correo())) throw new RuntimeException("Correo registrado");
        if (usuarioRepository.existsByTelefono(dto.telefono())) throw new RuntimeException("Teléfono registrado");
        if (competidorRepository.existsByDni(dni)) throw new RuntimeException("DNI registrado");

        // --- 2. VALIDACIÓN DEL CÓDIGO DE REGISTRO ---
        String codigoLimpio = codigoIngresado != null ? codigoIngresado.trim() : "";

        // Buscamos en la tabla de códigos
        CodigoRegistroCompetidor codigoEntidad = codigoRepo.findByCodigo(codigoLimpio)
                .orElseThrow(() -> new RuntimeException("El código de registro no existe: " + codigoLimpio));

        // Validamos si expiró (si tienes fecha de expiración)
        if (codigoEntidad.getExpiraEn() != null && codigoEntidad.getExpiraEn().before(new Date())) {
            throw new RuntimeException("Este código de invitación ha expirado.");
        }

        // Validamos si ya alcanzó el límite de usos
        if (codigoEntidad.getUsosActuales() >= codigoEntidad.getLimiteUso()) {
            throw new RuntimeException("Este código ya alcanzó su límite de usos.");
        }

        // Recuperamos el Club de ese código
        Club club = codigoEntidad.getClub();

        // Actualizamos el uso del código (Importante para tu lógica de negocio)
        codigoEntidad.setUsosActuales(codigoEntidad.getUsosActuales() + 1);
        if (codigoEntidad.getUsosActuales() >= codigoEntidad.getLimiteUso()) {
            codigoEntidad.setUsado(true);
        }
        codigoRepo.save(codigoEntidad);
        // ---------------------------------------------

        // --- 3. CREAR USUARIO ---
        Usuario usuario = Usuario.builder()
                .nombres(dto.nombres())
                .apellidos(dto.apellidos())
                .correo(dto.correo())
                .telefono(dto.telefono())
                .contrasenaHash(passwordEncoder.encode(dto.contrasena()))
                .rol("COMPETIDOR")
                .estado(EstadoUsuario.INACTIVO)
                .rol(RolUsuario.ADMINISTRADOR) // o el rol que definas
                .estado(EstadoUsuario.ACTIVO)
                .build();

        Usuario uGuardado = usuarioRepository.save(usuario);

        // --- 4. CREAR COMPETIDOR ---
        Competidor competidor = new Competidor();
        competidor.setUsuario(uGuardado);
        competidor.setClubActual(club); // ✅ Asignamos el club obtenido del código
        competidor.setDni(dni);
        competidor.setEstadoValidacion(EstadoValidacion.PENDIENTE);

        competidorRepository.save(competidor);

        return uGuardado;
    }

    @Transactional
    public Usuario actualizarUsuario(String id, CrearUsuarioDTO dto) {
        Usuario usuario = obtenerPorId(id);
        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setCorreo(dto.correo());
        usuario.setTelefono(dto.telefono());
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> login(String correo, String contrasena) {
        return usuarioRepository.findByCorreo(correo)
                .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO)
                .filter(u -> passwordEncoder.matches(contrasena, u.getContrasenaHash()));
    }

    public void eliminarUsuario(String id) {
        Usuario u = obtenerPorId(id);
        u.setEstado(EstadoUsuario.INACTIVO);
        usuarioRepository.save(u);
    }
}