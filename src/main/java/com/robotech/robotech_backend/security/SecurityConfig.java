package com.robotech.robotech_backend.security;

import com.robotech.robotech_backend.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Importante añadir
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 🔓 RUTAS PÚBLICAS
                        .requestMatchers(
                                "/api/usuarios/login",
                                "/api/admin/login",
                                "/api/usuarios",
                                "/api/auth/**",
                                "/api/codigos/validar/**",
                                "/uploads/**",
                                "/api/public/**" // ✅ NUEVA RUTA PÚBLICA PARA CLUBES
                        ).permitAll()

                        // Permitir GET a /api/public/clubes explícitamente por seguridad extra
                        .requestMatchers(HttpMethod.GET, "/api/public/clubes/**").permitAll()

                        .requestMatchers("/api/torneos/**").authenticated()
                        .anyRequest().authenticated()
                )

                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}