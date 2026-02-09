package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ActualizarEncuentroAdminDTO;
import com.robotech.robotech_backend.dto.CalificacionParticipanteDTO;
import com.robotech.robotech_backend.dto.CrearEncuentrosDTO;
import com.robotech.robotech_backend.dto.EncuentroAdminDTO;
import com.robotech.robotech_backend.dto.EncuentroDetalleJuezDTO;
import com.robotech.robotech_backend.dto.RegistrarResultadoEncuentroDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncuentroServiceTest {

    @Mock private CategoriaTorneoRepository categoriaRepo;
    @Mock private InscripcionTorneoRepository inscripcionRepo;
    @Mock private EquipoTorneoRepository equipoRepo;
    @Mock private EncuentroRepository encuentroRepo;
    @Mock private JuezRepository juezRepo;
    @Mock private ColiseoRepository coliseoRepo;
    @Mock private EncuentroParticipanteRepository encuentroParticipanteRepo;
    @Mock private HistorialCalificacionRepository historialRepo;
    @Mock private RobotRepository robotRepo;

    @InjectMocks
    private EncuentroService service;

    @Test
    void generarEncuentros_todos_contra_todos_ok() {
        CrearEncuentrosDTO dto = new CrearEncuentrosDTO();
        dto.setIdCategoriaTorneo("CAT1");
        dto.setTipoEncuentro(TipoEncuentro.TODOS_CONTRA_TODOS);
        dto.setIdJuez("J1");
        dto.setIdColiseo("C1");

        Torneo torneo = Torneo.builder().idTorneo("T1").nombre("Torneo A").build();
        CategoriaTorneo categoria = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT1")
                .torneo(torneo)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .categoria(CategoriaCompetencia.MINISUMO)
                .build();

        Usuario juezUser = Usuario.builder().idUsuario("U1").build();
        Juez juez = Juez.builder().idJuez("J1").usuario(juezUser).build();
        Coliseo coliseo = new Coliseo();
        coliseo.setIdColiseo("C1");
        coliseo.setNombre("Coliseo");

        Competidor c1 = Competidor.builder().idCompetidor("C1").estadoValidacion(EstadoValidacion.APROBADO).build();
        Competidor c2 = Competidor.builder().idCompetidor("C2").estadoValidacion(EstadoValidacion.APROBADO).build();
        Robot r1 = Robot.builder().idRobot("R1").estado(EstadoRobot.ACTIVO).competidor(c1).build();
        Robot r2 = Robot.builder().idRobot("R2").estado(EstadoRobot.ACTIVO).competidor(c2).build();
        InscripcionTorneo ins1 = InscripcionTorneo.builder().robot(r1).build();
        InscripcionTorneo ins2 = InscripcionTorneo.builder().robot(r2).build();

        when(categoriaRepo.findById("CAT1")).thenReturn(Optional.of(categoria));
        when(juezRepo.findById("J1")).thenReturn(Optional.of(juez));
        when(coliseoRepo.findById("C1")).thenReturn(Optional.of(coliseo));
        when(encuentroRepo.existsByCategoriaTorneoIdCategoriaTorneo("CAT1")).thenReturn(false);
        when(inscripcionRepo.findByCategoriaTorneoIdCategoriaTorneoAndEstado("CAT1", EstadoInscripcion.ACTIVADA)).thenReturn(List.of(ins1, ins2));
        when(robotRepo.findByCompetidor_IdCompetidor("U1")).thenReturn(List.of());
        when(encuentroRepo.save(any(Encuentro.class))).thenAnswer(inv -> inv.getArgument(0));

        List<EncuentroAdminDTO> result = service.generarEncuentros(dto);

        assertEquals(1, result.size());
        verify(encuentroParticipanteRepo, times(1)).saveAll(any(List.class));
    }

    @Test
    void generarEncuentros_eliminacion_directa_impar_lanza_error() {
        CrearEncuentrosDTO dto = new CrearEncuentrosDTO();
        dto.setIdCategoriaTorneo("CAT1");
        dto.setTipoEncuentro(TipoEncuentro.ELIMINACION_DIRECTA);
        dto.setIdJuez("J1");
        dto.setIdColiseo("C1");

        Torneo torneo = Torneo.builder().idTorneo("T1").nombre("Torneo A").build();
        CategoriaTorneo categoria = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT1")
                .torneo(torneo)
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .categoria(CategoriaCompetencia.MINISUMO)
                .build();

        Usuario juezUser = Usuario.builder().idUsuario("U1").build();
        Juez juez = Juez.builder().idJuez("J1").usuario(juezUser).build();
        Coliseo coliseo = new Coliseo();
        coliseo.setIdColiseo("C1");
        coliseo.setNombre("Coliseo");

        Competidor c1 = Competidor.builder().idCompetidor("C1").estadoValidacion(EstadoValidacion.APROBADO).build();
        Competidor c2 = Competidor.builder().idCompetidor("C2").estadoValidacion(EstadoValidacion.APROBADO).build();
        Competidor c3 = Competidor.builder().idCompetidor("C3").estadoValidacion(EstadoValidacion.APROBADO).build();
        Robot r1 = Robot.builder().idRobot("R1").estado(EstadoRobot.ACTIVO).competidor(c1).build();
        Robot r2 = Robot.builder().idRobot("R2").estado(EstadoRobot.ACTIVO).competidor(c2).build();
        Robot r3 = Robot.builder().idRobot("R3").estado(EstadoRobot.ACTIVO).competidor(c3).build();
        InscripcionTorneo ins1 = InscripcionTorneo.builder().robot(r1).build();
        InscripcionTorneo ins2 = InscripcionTorneo.builder().robot(r2).build();
        InscripcionTorneo ins3 = InscripcionTorneo.builder().robot(r3).build();

        when(categoriaRepo.findById("CAT1")).thenReturn(Optional.of(categoria));
        when(juezRepo.findById("J1")).thenReturn(Optional.of(juez));
        when(coliseoRepo.findById("C1")).thenReturn(Optional.of(coliseo));
        when(encuentroRepo.existsByCategoriaTorneoIdCategoriaTorneo("CAT1")).thenReturn(false);
        when(inscripcionRepo.findByCategoriaTorneoIdCategoriaTorneoAndEstado("CAT1", EstadoInscripcion.ACTIVADA))
                .thenReturn(List.of(ins1, ins2, ins3));
        when(robotRepo.findByCompetidor_IdCompetidor("U1")).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> service.generarEncuentros(dto));
    }

    @Test
    void generarEncuentros_ya_existen_lanza_error() {
        CrearEncuentrosDTO dto = new CrearEncuentrosDTO();
        dto.setIdCategoriaTorneo("CAT1");
        dto.setTipoEncuentro(TipoEncuentro.TODOS_CONTRA_TODOS);
        dto.setIdJuez("J1");
        dto.setIdColiseo("C1");

        CategoriaTorneo categoria = CategoriaTorneo.builder()
                .idCategoriaTorneo("CAT1")
                .modalidad(ModalidadCategoria.INDIVIDUAL)
                .build();

        when(categoriaRepo.findById("CAT1")).thenReturn(Optional.of(categoria));
        when(juezRepo.findById("J1")).thenReturn(Optional.of(Juez.builder().idJuez("J1").build()));
        when(coliseoRepo.findById("C1")).thenReturn(Optional.of(new Coliseo()));
        when(encuentroRepo.existsByCategoriaTorneoIdCategoriaTorneo("CAT1")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.generarEncuentros(dto));
    }

    @Test
    void actualizarEncuentroAdmin_ok() {
        Torneo torneo = Torneo.builder().nombre("Torneo A").build();
        CategoriaTorneo categoria = CategoriaTorneo.builder().torneo(torneo).categoria(CategoriaCompetencia.MINISUMO).build();

        Usuario juezUser = Usuario.builder().nombres("Ana").apellidos("Perez").build();
        Juez juez = Juez.builder().idJuez("J1").usuario(juezUser).build();
        Coliseo coliseo = new Coliseo();
        coliseo.setIdColiseo("C1");
        coliseo.setNombre("Coliseo");

        Encuentro encuentro = Encuentro.builder()
                .idEncuentro("E1")
                .categoriaTorneo(categoria)
                .juez(juez)
                .coliseo(coliseo)
                .estado(EstadoEncuentro.PROGRAMADO)
                .tipo(TipoEncuentro.TODOS_CONTRA_TODOS)
                .ronda(1)
                .fecha(new Date())
                .build();

        ActualizarEncuentroAdminDTO dto = new ActualizarEncuentroAdminDTO();
        dto.setIdJuez("J1");
        dto.setIdColiseo("C1");
        dto.setRonda(2);
        dto.setEstado(EstadoEncuentro.EN_CURSO);

        when(encuentroRepo.findById("E1")).thenReturn(Optional.of(encuentro));
        when(juezRepo.findById("J1")).thenReturn(Optional.of(juez));
        when(coliseoRepo.findById("C1")).thenReturn(Optional.of(coliseo));
        when(encuentroRepo.save(any(Encuentro.class))).thenAnswer(inv -> inv.getArgument(0));

        EncuentroAdminDTO resp = service.actualizarEncuentroAdmin("E1", dto);

        assertEquals(2, resp.getRonda());
        assertEquals(EstadoEncuentro.EN_CURSO, resp.getEstado());
    }

    @Test
    void actualizarEncuentroAdmin_finalizado_lanza_error() {
        Encuentro encuentro = Encuentro.builder()
                .idEncuentro("E1")
                .estado(EstadoEncuentro.FINALIZADO)
                .build();

        when(encuentroRepo.findById("E1")).thenReturn(Optional.of(encuentro));

        assertThrows(RuntimeException.class,
                () -> service.actualizarEncuentroAdmin("E1", new ActualizarEncuentroAdminDTO()));
    }

    @Test
    void registrarResultado_ok_finaliza_encuentro() {
        CategoriaTorneo categoria = CategoriaTorneo.builder().modalidad(ModalidadCategoria.INDIVIDUAL).build();
        Usuario juezUser = Usuario.builder().idUsuario("U1").build();
        Juez juez = Juez.builder().idJuez("J1").usuario(juezUser).build();
        Coliseo coliseo = new Coliseo();
        coliseo.setIdColiseo("C1");
        coliseo.setNombre("Coliseo");

        Encuentro encuentro = Encuentro.builder()
                .idEncuentro("E1")
                .categoriaTorneo(categoria)
                .juez(juez)
                .coliseo(coliseo)
                .estado(EstadoEncuentro.EN_CURSO)
                .tipo(TipoEncuentro.TODOS_CONTRA_TODOS)
                .ronda(1)
                .fecha(new Date())
                .build();

        EncuentroParticipante p1 = EncuentroParticipante.builder().idReferencia("R1").tipo(TipoParticipante.ROBOT).build();
        EncuentroParticipante p2 = EncuentroParticipante.builder().idReferencia("R2").tipo(TipoParticipante.ROBOT).build();

        RegistrarResultadoEncuentroDTO dto = new RegistrarResultadoEncuentroDTO();
        dto.setIdEncuentro("E1");
        CalificacionParticipanteDTO c1 = new CalificacionParticipanteDTO();
        c1.setIdReferencia("R1");
        c1.setCalificacion(10);
        CalificacionParticipanteDTO c2 = new CalificacionParticipanteDTO();
        c2.setIdReferencia("R2");
        c2.setCalificacion(5);
        dto.setCalificaciones(List.of(c1, c2));

        when(encuentroRepo.findById("E1")).thenReturn(Optional.of(encuentro));
        when(encuentroParticipanteRepo.findByEncuentroIdEncuentro("E1")).thenReturn(List.of(p1, p2));
        when(robotRepo.findByCompetidor_IdCompetidor("U1")).thenReturn(List.of());
        when(encuentroRepo.save(any(Encuentro.class))).thenAnswer(inv -> inv.getArgument(0));

        Encuentro guardado = service.registrarResultado("J1", dto);

        assertEquals(EstadoEncuentro.FINALIZADO, guardado.getEstado());
        assertEquals("R1", guardado.getGanadorIdReferencia());
        verify(historialRepo, times(2)).save(any(HistorialCalificacion.class));
    }

    @Test
    void registrarResultado_juez_incorrecto_lanza_error() {
        CategoriaTorneo categoria = CategoriaTorneo.builder().modalidad(ModalidadCategoria.INDIVIDUAL).build();
        Usuario juezUser = Usuario.builder().idUsuario("U1").build();
        Juez juez = Juez.builder().idJuez("J1").usuario(juezUser).build();
        Coliseo coliseo = new Coliseo();
        coliseo.setIdColiseo("C1");
        coliseo.setNombre("Coliseo");

        Encuentro encuentro = Encuentro.builder()
                .idEncuentro("E1")
                .categoriaTorneo(categoria)
                .juez(juez)
                .coliseo(coliseo)
                .estado(EstadoEncuentro.EN_CURSO)
                .tipo(TipoEncuentro.TODOS_CONTRA_TODOS)
                .ronda(1)
                .fecha(new Date())
                .build();

        RegistrarResultadoEncuentroDTO dto = new RegistrarResultadoEncuentroDTO();
        dto.setIdEncuentro("E1");
        dto.setCalificaciones(List.of());

        when(encuentroRepo.findById("E1")).thenReturn(Optional.of(encuentro));

        assertThrows(RuntimeException.class, () -> service.registrarResultado("J2", dto));
    }

    @Test
    void registrarResultado_empate_lanza_error() {
        CategoriaTorneo categoria = CategoriaTorneo.builder().modalidad(ModalidadCategoria.INDIVIDUAL).build();
        Usuario juezUser = Usuario.builder().idUsuario("U1").build();
        Juez juez = Juez.builder().idJuez("J1").usuario(juezUser).build();
        Coliseo coliseo = new Coliseo();
        coliseo.setIdColiseo("C1");
        coliseo.setNombre("Coliseo");

        Encuentro encuentro = Encuentro.builder()
                .idEncuentro("E1")
                .categoriaTorneo(categoria)
                .juez(juez)
                .coliseo(coliseo)
                .estado(EstadoEncuentro.EN_CURSO)
                .tipo(TipoEncuentro.TODOS_CONTRA_TODOS)
                .ronda(1)
                .fecha(new Date())
                .build();

        EncuentroParticipante p1 = EncuentroParticipante.builder().idReferencia("R1").tipo(TipoParticipante.ROBOT).build();
        EncuentroParticipante p2 = EncuentroParticipante.builder().idReferencia("R2").tipo(TipoParticipante.ROBOT).build();

        RegistrarResultadoEncuentroDTO dto = new RegistrarResultadoEncuentroDTO();
        dto.setIdEncuentro("E1");
        CalificacionParticipanteDTO c1 = new CalificacionParticipanteDTO();
        c1.setIdReferencia("R1");
        c1.setCalificacion(10);
        CalificacionParticipanteDTO c2 = new CalificacionParticipanteDTO();
        c2.setIdReferencia("R2");
        c2.setCalificacion(10);
        dto.setCalificaciones(List.of(c1, c2));

        when(encuentroRepo.findById("E1")).thenReturn(Optional.of(encuentro));
        when(encuentroParticipanteRepo.findByEncuentroIdEncuentro("E1")).thenReturn(List.of(p1, p2));
        when(robotRepo.findByCompetidor_IdCompetidor("U1")).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> service.registrarResultado("J1", dto));
    }

    @Test
    void registrarResultado_juez_participa_lanza_error() {
        CategoriaTorneo categoria = CategoriaTorneo.builder().modalidad(ModalidadCategoria.INDIVIDUAL).build();
        Usuario juezUser = Usuario.builder().idUsuario("U1").build();
        Juez juez = Juez.builder().idJuez("J1").usuario(juezUser).build();
        Coliseo coliseo = new Coliseo();
        coliseo.setIdColiseo("C1");
        coliseo.setNombre("Coliseo");

        Encuentro encuentro = Encuentro.builder()
                .idEncuentro("E1")
                .categoriaTorneo(categoria)
                .juez(juez)
                .coliseo(coliseo)
                .estado(EstadoEncuentro.EN_CURSO)
                .tipo(TipoEncuentro.TODOS_CONTRA_TODOS)
                .ronda(1)
                .fecha(new Date())
                .build();

        EncuentroParticipante p1 = EncuentroParticipante.builder().idReferencia("R1").tipo(TipoParticipante.ROBOT).build();
        EncuentroParticipante p2 = EncuentroParticipante.builder().idReferencia("R2").tipo(TipoParticipante.ROBOT).build();

        RegistrarResultadoEncuentroDTO dto = new RegistrarResultadoEncuentroDTO();
        dto.setIdEncuentro("E1");
        CalificacionParticipanteDTO c1 = new CalificacionParticipanteDTO();
        c1.setIdReferencia("R1");
        c1.setCalificacion(10);
        CalificacionParticipanteDTO c2 = new CalificacionParticipanteDTO();
        c2.setIdReferencia("R2");
        c2.setCalificacion(5);
        dto.setCalificaciones(List.of(c1, c2));

        when(encuentroRepo.findById("E1")).thenReturn(Optional.of(encuentro));
        when(encuentroParticipanteRepo.findByEncuentroIdEncuentro("E1")).thenReturn(List.of(p1, p2));
        when(robotRepo.findByCompetidor_IdCompetidor("U1")).thenReturn(List.of(Robot.builder().idRobot("R1").build()));

        assertThrows(RuntimeException.class, () -> service.registrarResultado("J1", dto));
    }

    @Test
    void obtenerDetalleParaJuez_ok() {
        CategoriaTorneo categoria = CategoriaTorneo.builder().modalidad(ModalidadCategoria.INDIVIDUAL).build();
        Usuario juezUser = Usuario.builder().idUsuario("U1").build();
        Juez juez = Juez.builder().idJuez("J1").usuario(juezUser).build();
        Coliseo coliseo = new Coliseo();
        coliseo.setIdColiseo("C1");
        coliseo.setNombre("Coliseo");
        Encuentro encuentro = Encuentro.builder()
                .idEncuentro("E1")
                .categoriaTorneo(categoria)
                .juez(juez)
                .coliseo(coliseo)
                .estado(EstadoEncuentro.EN_CURSO)
                .tipo(TipoEncuentro.TODOS_CONTRA_TODOS)
                .ronda(1)
                .fecha(new Date())
                .build();

        EncuentroParticipante p1 = EncuentroParticipante.builder().idReferencia("R1").tipo(TipoParticipante.ROBOT).build();

        when(juezRepo.findByUsuario_IdUsuario("U1")).thenReturn(Optional.of(juez));
        when(encuentroRepo.findById("E1")).thenReturn(Optional.of(encuentro));
        when(encuentroParticipanteRepo.findByEncuentroIdEncuentro("E1")).thenReturn(List.of(p1));
        when(robotRepo.findById("R1")).thenReturn(Optional.of(Robot.builder().idRobot("R1").nombre("Titan").build()));

        EncuentroDetalleJuezDTO detalle = service.obtenerDetalleParaJuez("U1", "E1");

        assertNotNull(detalle);
        assertEquals(1, detalle.participantes().size());
    }
}
