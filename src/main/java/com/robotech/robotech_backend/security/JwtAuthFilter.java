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

        // 🔥 EXCLUIR ENDPOINTS DE LOGIN
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

            if (usuario.getRol() == RolUsuario.CLUB || usuario.getRol() == RolUsuario.CLUB_COMPETIDOR) {
                Club club = clubRepository.findByUsuarioIdUsuarioFetch(usuario.getIdUsuario()).orElse(null);
                if (club == null || club.getEstado() != EstadoClub.ACTIVO) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            boolean competidorOk = false;
            if (usuario.getRol() == RolUsuario.COMPETIDOR || usuario.getRol() == RolUsuario.CLUB_COMPETIDOR) {
                Competidor comp = competidorRepository.findByUsuarioIdUsuarioFetch(usuario.getIdUsuario()).orElse(null);
                competidorOk = comp != null
                        && comp.getEstadoValidacion() == EstadoValidacion.APROBADO
                        && comp.getClubActual() != null
                        && comp.getClubActual().getEstado() == EstadoClub.ACTIVO;
                if (usuario.getRol() == RolUsuario.COMPETIDOR && !competidorOk) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (usuario.getRol() == RolUsuario.CLUB_COMPETIDOR) {
                authorities.add(new SimpleGrantedAuthority("ROLE_CLUB_COMPETIDOR"));
                authorities.add(new SimpleGrantedAuthority("CLUB_COMPETIDOR"));
                authorities.add(new SimpleGrantedAuthority("ROLE_CLUB"));
                authorities.add(new SimpleGrantedAuthority("CLUB"));
                if (competidorOk) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_COMPETIDOR"));
                    authorities.add(new SimpleGrantedAuthority("COMPETIDOR"));
                }
            } else {
                String roleName = usuario.getRol().name();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                authorities.add(new SimpleGrantedAuthority(roleName));
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
