package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.JuezEstadoDTO;
import com.robotech.robotech_backend.dto.JuezPostulacionDTO;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Juez;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.JuezRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostulacionJuezServiceTest {

    @Mock private JuezRepository juezRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private CompetidorRepository competidorRepo;

    @InjectMocks
    private PostulacionJuezService service;

    @Test
    void obtenerEstado_sin_juez_retorna_null() {
        when(juezRepo.findByUsuario_IdUsuario("U1")).thenReturn(Optional.empty());
        assertEquals(null, service.obtenerEstado("U1"));
    }

    @Test
    void postular_ok_crea_juez() {
        JuezPostulacionDTO dto = new JuezPostulacionDTO();
        dto.setLicencia("LIC-01");

        Usuario usuario = Usuario.builder().idUsuario("U1").build();
        Competidor comp = Competidor.builder().idCompetidor("U1").estadoValidacion(EstadoValidacion.APROBADO).build();

        when(usuarioRepo.findById("U1")).thenReturn(Optional.of(usuario));
        when(competidorRepo.findByUsuario_IdUsuario("U1")).thenReturn(Optional.of(comp));
        when(juezRepo.findByUsuario_IdUsuario("U1")).thenReturn(Optional.empty());
        when(juezRepo.findByLicencia("LIC-01")).thenReturn(Optional.empty());
        when(juezRepo.save(any(Juez.class))).thenAnswer(inv -> inv.getArgument(0));

        JuezEstadoDTO estado = service.postular("U1", dto);

        assertNotNull(estado);
        assertEquals(EstadoValidacion.PENDIENTE, estado.getEstado());
    }

    @Test
    void postular_ya_es_juez_aprobado_lanza_error() {
        JuezPostulacionDTO dto = new JuezPostulacionDTO();
        dto.setLicencia("LIC-01");

        Usuario usuario = Usuario.builder().idUsuario("U1").build();
        Competidor comp = Competidor.builder().idCompetidor("U1").estadoValidacion(EstadoValidacion.APROBADO).build();
        Juez juez = Juez.builder().idJuez("J1").estadoValidacion(EstadoValidacion.APROBADO).build();

        when(usuarioRepo.findById("U1")).thenReturn(Optional.of(usuario));
        when(competidorRepo.findByUsuario_IdUsuario("U1")).thenReturn(Optional.of(comp));
        when(juezRepo.findByUsuario_IdUsuario("U1")).thenReturn(Optional.of(juez));
        when(juezRepo.findByLicencia("LIC-01")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.postular("U1", dto));
    }
}
