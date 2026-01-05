package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.RobotDTO;
import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Competidor;
import com.robotech.robotech_backend.model.Robot;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RobotService {

    private final RobotRepository robotRepo;
    private final CompetidorRepository competidorRepo;
    private final NicknameValidator nicknameValidator;

    public Robot crearRobot(String idCompetidor, RobotDTO dto) {

        nicknameValidator.validar(dto.getNickname());

        Competidor comp = competidorRepo.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        if (robotRepo.existsByCompetidor_IdCompetidorAndCategoria(idCompetidor, dto.getCategoria().name())) {
            throw new RuntimeException("Ya tienes un robot registrado en esta categoría");
        }

        if (robotRepo.existsByNickname(dto.getNickname())) {
            throw new RuntimeException("Este nickname ya está en uso");
        }

        Robot robot = Robot.builder()
                .nombre(dto.getNombre())
                .categoria(dto.getCategoria())
                .nickname(dto.getNickname())
                .competidor(comp)
                .build();

        return robotRepo.save(robot);
    }

    public List<Robot> listarPorCompetidor(String idCompetidor) {
        return robotRepo.findByCompetidor_IdCompetidor(idCompetidor);
    }

    public Robot editarRobot(String idRobot, RobotDTO dto) {

        Robot robot = robotRepo.findById(idRobot)
                .orElseThrow(() -> new RuntimeException("Robot no existe"));

        // validar nickname nuevo
        nicknameValidator.validar(dto.getNickname());

        robot.setNombre(dto.getNombre());
        robot.setCategoria(dto.getCategoria());
        robot.setNickname(dto.getNickname());

        return robotRepo.save(robot);
    }

    public void eliminar(String idRobot) {
        robotRepo.deleteById(idRobot);
    }


    public List<Robot> listarPorClub(Club club) {
        return robotRepo.findByCompetidor_Club(club);
    }


}

