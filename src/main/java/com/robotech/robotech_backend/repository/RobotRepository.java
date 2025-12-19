package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Robot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RobotRepository extends JpaRepository<Robot, String> {

    // Obtener todos los robots de un competidor
    List<Robot> findByCompetidor_IdCompetidor(String idCompetidor);

    // Verificar si ya registró un robot en la categoría
    boolean existsByCompetidor_IdCompetidorAndCategoria(String idCompetidor, String categoria);

    // Verificar si el nickname ya está en uso
    boolean existsByNickname(String nickname);
    // 🔥 ESTE ES EL BUENO CON TU MODELO
    List<Robot> findByCompetidor_Club(Club club);

    int countByCompetidor_IdCompetidor(String idCompetidor);

}
