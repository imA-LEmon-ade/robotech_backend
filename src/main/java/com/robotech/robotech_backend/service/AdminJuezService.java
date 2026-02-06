package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.JuezAdminDTO;
import com.robotech.robotech_backend.dto.JuezDTO;
import com.robotech.robotech_backend.dto.JuezSelectDTO;
import com.robotech.robotech_backend.dto.UsuarioDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.JuezRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.repository.EncuentroRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminJuezService {

    private final JuezRepository juezRepository;
    private final CompetidorRepository competidorRepository;
    private final UsuarioRepository usuarioRepository;
    private final EncuentroRepository encuentroRepository;
    private final PasswordEncoder passwordEncoder;
    private final DniValidator dniValidator;
    private final TelefonoValidator telefonoValidator;

    // ---------------------------------------------------------
    // LISTAR (CORREGIDO PARA TRAER NOMBRES)
    // ---------------------------------------------------------
    public Page<JuezAdminDTO> listar(Pageable pageable, String q) {
        String term = (q == null || q.isBlank()) ? null : q.trim();
        return juezRepository.buscar(term, pageable)
                .map(j -> new JuezAdminDTO(
                        j.getIdJuez(),
                        j.getLicencia(),
                        j.getEstadoValidacion(),
                        new UsuarioDTO(
                                j.getUsuario().getIdUsuario(),
                                j.getUsuario().getDni(),
                                j.getUsuario().getNombres(),
                                j.getUsuario().getApellidos(),
                                j.getUsuario().getCorreo(),
                                j.getUsuario().getRoles(),
                                j.getUsuario().getEstado(),
                                j.getUsuario().getTelefono()
                        )
                ));
    }




    // ---------------------------------------------------------
    // CREAR JUEZ
    // ---------------------------------------------------------
    @Transactional
    public Juez crear(JuezDTO dto) {
        dniValidator.validar(dto.getDni());
        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            telefonoValidator.validar(dto.getTelefono());
        }
        Usuario u = Usuario.builder()
                .dni(dto.getDni())
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .roles(Set.of(RolUsuario.JUEZ, RolUsuario.COMPETIDOR))
                .estado(EstadoUsuario.ACTIVO)
                .build();

        usuarioRepository.save(u);

        Competidor comp = competidorRepository.findByUsuario_IdUsuario(u.getIdUsuario()).orElse(null);
        if (comp == null) {
            comp = Competidor.builder()
                    .usuario(u)
                    .clubActual(null)
                    .estadoValidacion(EstadoValidacion.APROBADO)
                    .build();
            competidorRepository.save(comp);
        } else {
            comp.setClubActual(null);
            comp.setEstadoValidacion(EstadoValidacion.APROBADO);
            competidorRepository.save(comp);
        }

        Juez j = Juez.builder()
                .usuario(u)
                .licencia(dto.getLicencia())
                .estadoValidacion(EstadoValidacion.APROBADO)
                .creadoPor(dto.getCreadoPor())
                .creadoEn(new Date())
                .validadoPor(dto.getCreadoPor())
                .validadoEn(new Date())
                .build();

        return juezRepository.save(j);
    }

    // ---------------------------------------------------------
    // EDITAR JUEZ
    // ---------------------------------------------------------
    @Transactional
    public Juez editar(String id, JuezDTO dto) {
        Juez j = juezRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        Usuario u = j.getUsuario();
        dniValidator.validar(dto.getDni());
        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            telefonoValidator.validar(dto.getTelefono());
        }
        u.setDni(dto.getDni());
        u.setNombres(dto.getNombres());
        u.setApellidos(dto.getApellidos());
        u.setCorreo(dto.getCorreo());
        u.setTelefono(dto.getTelefono());

        if (dto.getContrasena() != null && !dto.getContrasena().isBlank()) {
            u.setContrasenaHash(passwordEncoder.encode(dto.getContrasena()));
        }

        usuarioRepository.save(u);
        j.setLicencia(dto.getLicencia());

        return juezRepository.save(j);
    }

    // ---------------------------------------------------------
    // LISTAR PARA SELECT (OPTIMIZADO)
    // ---------------------------------------------------------
    public List<JuezSelectDTO> listarJuecesParaSelect() {
        // ✅ También usa fetch en el repositorio para traer los nombres
        return juezRepository.findByEstadoValidacion(EstadoValidacion.APROBADO)
                .stream()
                .map(j -> new JuezSelectDTO(
                        j.getIdJuez(),
                        (j.getUsuario().getNombres() != null ? j.getUsuario().getNombres() : "") + " " +
                                (j.getUsuario().getApellidos() != null ? j.getUsuario().getApellidos() : "")
                ))
                .toList();
    }

    // ---------------------------------------------------------
    // OTROS MÉTODOS (SIN CAMBIOS)
    // ---------------------------------------------------------
    public void eliminar(String id) {
        Juez juez = juezRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Juez no existe"));
        Usuario u = juez.getUsuario();
        juezRepository.delete(juez);
        if (u != null) {
            usuarioRepository.delete(u);
        }
    }

    public Juez aprobar(String idJuez, String adminId) {
        Juez j = juezRepository.findById(idJuez)
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));
        j.setEstadoValidacion(EstadoValidacion.APROBADO);
        j.setValidadoPor(adminId);
        j.setValidadoEn(new Date());

        Usuario u = j.getUsuario();
        if (u != null) {
            u.setEstado(EstadoUsuario.ACTIVO);
            u.getRoles().add(RolUsuario.JUEZ);

            Competidor comp = competidorRepository.findByUsuario_IdUsuario(u.getIdUsuario()).orElse(null);
            if (comp == null) {
                comp = Competidor.builder()
                        .usuario(u)
                        .clubActual(null)
                        .estadoValidacion(EstadoValidacion.APROBADO)
                        .build();
                competidorRepository.save(comp);
            } else {
                comp.setClubActual(null);
                comp.setEstadoValidacion(EstadoValidacion.APROBADO);
                competidorRepository.save(comp);
            }
            u.getRoles().add(RolUsuario.COMPETIDOR);

            usuarioRepository.save(u);
        }

        return juezRepository.save(j);
    }

    
    public Juez inactivar(String idJuez, String adminId) {
        Juez j = juezRepository.findById(idJuez)
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        if (j.getEstadoValidacion() != EstadoValidacion.APROBADO) {
            throw new RuntimeException("El juez no est? aprobado");
        }

        long pendientes = encuentroRepository.countByJuezIdJuezAndEstadoNot(
                j.getIdJuez(),
                EstadoEncuentro.FINALIZADO
        );

        if (pendientes > 0) {
            throw new RuntimeException("El juez tiene encuentros pendientes por calificar");
        }

        Usuario u = j.getUsuario();
        if (u != null) {
            u.getRoles().remove(RolUsuario.JUEZ);
            usuarioRepository.save(u);
        }

        j.setEstadoValidacion(EstadoValidacion.RECHAZADO);
        j.setValidadoPor(adminId);
        j.setValidadoEn(new Date());

        return juezRepository.save(j);
    }

    public Juez rechazar(String idJuez, String adminId) {
        Juez j = juezRepository.findById(idJuez)
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));
        j.setEstadoValidacion(EstadoValidacion.RECHAZADO);
        j.setValidadoPor(adminId);
        j.setValidadoEn(new Date());
        return juezRepository.save(j);
    }
}


