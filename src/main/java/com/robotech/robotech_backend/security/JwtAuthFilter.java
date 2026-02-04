package com.robotech.robotech_backend.security;

import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Competidor;
import com.robotech.robotech_backend.model.EstadoClub;
import com.robotech.robotech_backend.model.EstadoUsuario;
import com.robotech.robotech_backend.model.EstadoValidacion;
import com.robotech.robotech_backend.model.RolUsuario;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final ClubRepository clubRepository;
    private final CompetidorRepository competidorRepository;

    public JwtAuthFilter(JwtService jwtService,
                         UsuarioRepository usuarioRepository,
                         ClubRepository clubRepository,
                         CompetidorRepository competidorRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.clubRepository = clubRepository;
        this.competidorRepository = competidorRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ?? EXCLUIR ENDPOINTS DE LOGIN
        if (
                path.equals("/api/admin/login") ||
                        path.equals("/api/usuarios/login") ||
                        path.startsWith("/api/auth")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtService.esTokenValido(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String correo = jwtService.obtenerCorreo(token);

        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

        if (usuario != null) {
            if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
                filterChain.doFilter(request, response);
                return;
            }

            Set<RolUsuario> roles = usuario.getRoles() != null ? usuario.getRoles() : java.util.Set.of();

            if (roles.contains(RolUsuario.CLUB)) {
                Club club = clubRepository.findByUsuarioIdUsuarioFetch(usuario.getIdUsuario()).orElse(null);
                if (club == null || club.getEstado() != EstadoClub.ACTIVO) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            boolean competidorOk = false;
            if (roles.contains(RolUsuario.COMPETIDOR)) {
                Competidor comp = competidorRepository.findByUsuarioIdUsuarioFetch(usuario.getIdUsuario()).orElse(null);
                boolean clubValido = comp != null
                        && comp.getClubActual() != null
                        && comp.getClubActual().getEstado() == EstadoClub.ACTIVO;
                boolean libre = comp != null && comp.getClubActual() == null;

                competidorOk = comp != null
                        && comp.getEstadoValidacion() == EstadoValidacion.APROBADO
                        && (clubValido || roles.contains(RolUsuario.JUEZ) || libre);

                if (!competidorOk) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (RolUsuario rol : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.name()));
                authorities.add(new SimpleGrantedAuthority(rol.name()));
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }


        filterChain.doFilter(request, response);
    }
}
