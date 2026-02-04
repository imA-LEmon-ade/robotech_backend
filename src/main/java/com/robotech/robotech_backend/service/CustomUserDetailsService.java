package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepo;

    @Override
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepo.findByCorreo(correo)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado"));

        java.util.Set<com.robotech.robotech_backend.model.RolUsuario> roles =
                (usuario.getRoles() != null ? usuario.getRoles() : java.util.Collections.emptySet());

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .flatMap(r -> List.of(
                        new SimpleGrantedAuthority("ROLE_" + r.name()),
                        new SimpleGrantedAuthority(r.name())
                ).stream())
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getContrasenaHash())
                .authorities(authorities)
                .build();
    }
}
