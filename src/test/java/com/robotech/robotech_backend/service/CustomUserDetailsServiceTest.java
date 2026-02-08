package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UsuarioRepository usuarioRepo;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_ok() {
        Usuario usuario = Usuario.builder()
                .correo("ana@robotech.com")
                .contrasenaHash("hash")
                .roles(Set.of(RolUsuario.ADMINISTRADOR))
                .build();

        when(usuarioRepo.findByCorreo("ana@robotech.com")).thenReturn(Optional.of(usuario));

        UserDetails details = service.loadUserByUsername("ana@robotech.com");

        assertEquals("ana@robotech.com", details.getUsername());
        assertEquals("hash", details.getPassword());
    }

    @Test
    void loadUserByUsername_no_encontrado() {
        when(usuarioRepo.findByCorreo("x@x.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("x@x.com"));
    }
}
