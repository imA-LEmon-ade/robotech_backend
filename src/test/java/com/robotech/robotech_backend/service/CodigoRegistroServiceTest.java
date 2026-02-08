package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.entity.CodigoRegistroCompetidor;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.repository.CodigoRegistroCompetidorRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodigoRegistroServiceTest {

    @Mock private CodigoRegistroCompetidorRepository codigoRepo;
    @Mock private ClubRepository clubRepository;

    @InjectMocks
    private CodigoRegistroService service;

    @Test
    void generarCodigoParaClub_ok() {
        Club club = Club.builder().idClub("C1").build();
        when(clubRepository.findById("C1")).thenReturn(Optional.of(club));
        when(codigoRepo.save(any(CodigoRegistroCompetidor.class))).thenAnswer(inv -> inv.getArgument(0));

        CodigoRegistroCompetidor codigo = service.generarCodigoParaClub("C1", 1, 1);

        assertEquals(club, codigo.getClub());
    }

    @Test
    void validarCodigo_expirado_lanza_error() {
        CodigoRegistroCompetidor codigo = CodigoRegistroCompetidor.builder()
                .codigo("ABC")
                .expiraEn(new Date(System.currentTimeMillis() - 1000))
                .limiteUso(1)
                .usosActuales(0)
                .usado(false)
                .build();

        when(codigoRepo.findByCodigo("ABC")).thenReturn(Optional.of(codigo));

        assertThrows(RuntimeException.class, () -> service.validarCodigo("ABC"));
    }

    @Test
    void marcarUso_actualiza_contador() {
        CodigoRegistroCompetidor codigo = CodigoRegistroCompetidor.builder()
                .codigo("ABC")
                .limiteUso(1)
                .usosActuales(0)
                .usado(false)
                .build();

        service.marcarUso(codigo);

        assertEquals(1, codigo.getUsosActuales());
        assertEquals(true, codigo.isUsado());
        verify(codigoRepo, times(1)).save(codigo);
    }
}
