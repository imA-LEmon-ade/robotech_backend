package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearJuezDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.JuezRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.service.validadores.EmailValidator;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class SubAdminJuezService {

    private final JuezRepository juezRepo;
    private final UsuarioRepository usuarioRepo;
    private final CompetidorRepository competidorRepo;
    private final PasswordEncoder passwordEncoder;

    private final EmailValidator emailValidator;
    private final TelefonoValidator telefonoValidator;
    private final DniValidator dniValidator;

    public void crearJuez(CrearJuezDTO dto) {

        String correo = dto.getCorreo().trim().toLowerCase();

        emailValidator.validar(correo);
        telefonoValidator.validar(dto.getTelefono());
        dniValidator.validar(dto.getDni());

        if (usuarioRepo.existsByCorreoIgnoreCase(correo)) {
            throw new RuntimeException("Correo del juez ya registrado");
        }

        if (usuarioRepo.existsByTelefono(dto.getTelefono())) {
            throw new RuntimeException("Teléfono del juez ya registrado");
        }

        if (usuarioRepo.existsByDni(dto.getDni())) {
            throw new RuntimeException("DNI del juez ya registrado");
        }

        if (juezRepo.existsByUsuario_IdUsuario(dto.getDni())) {
            throw new RuntimeException("Juez ya existe");
        }

        Usuario usuario = Usuario.builder()
                .dni(dto.getDni())
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .correo(correo)
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .roles(Set.of(RolUsuario.JUEZ, RolUsuario.COMPETIDOR))
                .estado(EstadoUsuario.ACTIVO)
                .build();

        usuarioRepo.save(usuario);

        Competidor comp = Competidor.builder()
                .usuario(usuario)
                .clubActual(null)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();
        competidorRepo.save(comp);

        Juez juez = Juez.builder()
                .usuario(usuario)
                .licencia(dto.getLicencia())
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();

        juezRepo.save(juez);
    }
}
