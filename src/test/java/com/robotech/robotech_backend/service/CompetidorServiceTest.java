package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CompetidorActualizarDTO;
import com.robotech.robotech_backend.dto.CompetidorClubDTO;
import com.robotech.robotech_backend.dto.CompetidorPerfilDTO;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Juez;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoEncuentro;
import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.EncuentroRepository;
import com.robotech.robotech_backend.repository.JuezRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetidorServiceTest {

    @Mock private CompetidorRepository competidorRepo;
    @Mock private RobotRepository robotRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private JuezRepository juezRepo;
    @Mock private EncuentroRepository encuentroRepo;
    @Mock private DniValidator dniValidator;
    @Mock private TelefonoValidator telefonoValidator;

    @InjectMocks
    private CompetidorService competidorService;

    @Test
    void obtenerPerfil_mapea_campos() {
        Usuario usuario = Usuario.builder()
                .idUsuario("U1")
                .nombres("Ana")
                .apellidos("Perez")
                .dni("12345678")
                .correo("ana@robotech.com")
                .telefono("999111222")
                .build();
        Club club = Club.builder().idClub("C1").nombre("Club A").build();
        Competidor comp = Competidor.builder()
                .idCompetidor("U1")
                .usuario(usuario)
                .clubActual(club)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .fotoUrl("/uploads/competidores/f.jpg")
                .build();

        when(competidorRepo.findById("U1")).thenReturn(Optional.of(comp));
        when(robotRepo.countByCompetidor_IdCompetidor("U1")).thenReturn(3);

        CompetidorPerfilDTO dto = competidorService.obtenerPerfil("U1");

        assertEquals("Ana", dto.getNombres());
        assertEquals("Perez", dto.getApellidos());
        assertEquals("Club A", dto.getClubNombre());
        assertEquals(3, dto.getTotalRobots());
        assertEquals("/uploads/competidores/f.jpg", dto.getFotoUrl());
    }

    @Test
    void actualizarPerfil_actualiza_usuario_y_correo() {
        Usuario usuario = Usuario.builder()
                .idUsuario("U1")
                .nombres("Ana")
                .apellidos("Perez")
                .dni("12345678")
                .correo("old@robotech.com")
                .telefono("999111222")
                .estado(EstadoUsuario.ACTIVO)
                .build();
        Competidor comp = Competidor.builder()
                .idCompetidor("U1")
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();

        CompetidorActualizarDTO dto = new CompetidorActualizarDTO();
        dto.setNombres("Ana Maria");
        dto.setApellidos("Perez");
        dto.setTelefono("999111222");
        dto.setCorreo("new@robotech.com");
        dto.setDni("12345678");

        when(competidorRepo.findById("U1")).thenReturn(Optional.of(comp));
        when(usuarioRepo.existsByCorreo("new@robotech.com")).thenReturn(false);

        competidorService.actualizarPerfil("U1", dto);

        assertEquals("Ana Maria", usuario.getNombres());
        assertEquals("new@robotech.com", usuario.getCorreo());
        verify(competidorRepo, times(1)).save(comp);
    }

    @Test
    void listarPorClub_filtra_por_busqueda() {
        Usuario u1 = Usuario.builder().nombres("Ana").apellidos("Perez").dni("123").correo("ana@x.com").build();
        Usuario u2 = Usuario.builder().nombres("Luis").apellidos("Gomez").dni("456").correo("luis@x.com").build();
        Competidor c1 = Competidor.builder().idCompetidor("C1").usuario(u1).estadoValidacion(EstadoValidacion.APROBADO).build();
        Competidor c2 = Competidor.builder().idCompetidor("C2").usuario(u2).estadoValidacion(EstadoValidacion.APROBADO).build();

        when(competidorRepo.findByClubActual_IdClub("CLUB")).thenReturn(List.of(c1, c2));

        List<CompetidorClubDTO> result = competidorService.listarPorClub("CLUB", "ana");

        assertEquals(1, result.size());
        assertEquals("C1", result.get(0).idCompetidor());
    }

    @Test
    void aprobarCompetidor_juez_pendientes_lanza_error() {
        Usuario usuario = Usuario.builder()
                .idUsuario("U1")
                .roles(new HashSet<>(Set.of(RolUsuario.JUEZ)))
                .estado(EstadoUsuario.ACTIVO)
                .build();
        Competidor competidor = Competidor.builder()
                .idCompetidor("U1")
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .build();
        Juez juez = Juez.builder()
                .idJuez("J1")
                .usuario(usuario)
                .estadoValidacion(EstadoValidacion.APROBADO)
                .build();

        when(competidorRepo.findById("U1")).thenReturn(Optional.of(competidor));
        when(juezRepo.findByUsuario_IdUsuario("U1")).thenReturn(Optional.of(juez));
        when(encuentroRepo.countByJuezIdJuezAndEstadoNot("J1", EstadoEncuentro.FINALIZADO)).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> competidorService.aprobarCompetidor("U1"));
        verify(juezRepo, never()).save(any(Juez.class));
    }

    @Test
    void rechazarCompetidor_actualiza_estado() {
        Competidor competidor = Competidor.builder()
                .idCompetidor("U1")
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .build();

        when(competidorRepo.findById("U1")).thenReturn(Optional.of(competidor));

        competidorService.rechazarCompetidor("U1");

        assertEquals(EstadoValidacion.RECHAZADO, competidor.getEstadoValidacion());
        verify(competidorRepo, times(1)).save(competidor);
    }
}
