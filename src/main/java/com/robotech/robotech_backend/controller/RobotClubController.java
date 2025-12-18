package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Robot;
import com.robotech.robotech_backend.repository.TorneoRepository;
import com.robotech.robotech_backend.service.ClubService;
import com.robotech.robotech_backend.service.RobotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/club/robots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLUB')")
@CrossOrigin("*")
public class RobotClubController {

    private final RobotService robotService;
    private final ClubService clubService;

    @GetMapping
    public List<Robot> misRobots(Authentication auth) {
        Club club = clubService.obtenerPorUsuario(auth);
        return robotService.listarPorClub(club);
    }
}
