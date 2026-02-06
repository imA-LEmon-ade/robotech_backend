package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.entity.CodigoRegistroCompetidor;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.repository.CodigoRegistroCompetidorRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CodigoRegistroService {

    private final CodigoRegistroCompetidorRepository codigoRepo;
    private final ClubRepository clubRepository;

    /**
     * Generar un código con configuración personalizada.
     * @param idClub ID del club que genera el código
     * @param horasExpiracion horas para expirar el código
     * @param limiteUso cantidad de usos permitidos (para ti será 1)
     */
    public CodigoRegistroCompetidor generarCodigoParaClub(String idClub, int horasExpiracion, int limiteUso) {

        Club club = clubRepository.findById(idClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        // Calcular fecha de expiración
        Date fechaExpiracion = new Date(System.currentTimeMillis() + (long) horasExpiracion * 60 * 60 * 1000);

        CodigoRegistroCompetidor codigo = CodigoRegistroCompetidor.builder()
                .club(club)
                .expiraEn(fechaExpiracion)
                .limiteUso(limiteUso)
                .usosActuales(0)
                .usado(false)
                .build();

        return codigoRepo.save(codigo);
    }

    /**
     * Validar un código de registro
     */
    public CodigoRegistroCompetidor validarCodigo(String codigo) {

        CodigoRegistroCompetidor c = codigoRepo.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Código no existe"));

        // ¿Expiró?
        if (c.getExpiraEn().before(new Date())) {
            throw new RuntimeException("Código expirado");
        }

        // ¿Se usó totalmente?
        if (c.isUsado()) {
            throw new RuntimeException("Código ya fue utilizado");
        }

        // ¿Alcanzó límite de uso?
        if (c.getUsosActuales() >= c.getLimiteUso()) {
            throw new RuntimeException("Límite de uso alcanzado");
        }

        return c;
    }

    /**
     * Actualizar uso del código (cuando el competidor se registra)
     */
    public void marcarUso(CodigoRegistroCompetidor codigo) {
        int actual = codigo.getUsosActuales() + 1;
        codigo.setUsosActuales(actual);

        if (actual >= codigo.getLimiteUso()) {
            codigo.setUsado(true);
        }

        codigoRepo.save(codigo);
    }

    public List<CodigoRegistroCompetidor> listarCodigosPorClub(String idClub) {
        return codigoRepo.findByClub_IdClub(idClub);
    }


}


