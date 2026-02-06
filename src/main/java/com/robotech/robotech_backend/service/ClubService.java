package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final UsuarioRepository usuarioRepository;
    private final RobotRepository robotRepository;
    // ✅ Añadimos el repositorio con el nombre exacto que proporcionaste
    private final CodigoRegistroCompetidorRepository codigoRegistroCompetidorRepository;

    public List<Club> listar() {
        return clubRepository.findAll();
    }

    public Optional<Club> obtener(String id) {
        return clubRepository.findById(id);
    }

    public Club crear(Club club) {
        Usuario usuarioReal = usuarioRepository.findById(club.getUsuario().getIdUsuario())
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));
        club.setUsuario(usuarioReal);
        return clubRepository.save(club);
    }

    public void eliminar(String id) {
        clubRepository.deleteById(id);
    }

    public Club obtenerPorUsuario(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return clubRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Club no asociado al usuario"));
    }

    // =============================================================
    // 📊 LÓGICA DE ESTADÍSTICAS PARA EL DASHBOARD
    // =============================================================

    /**
     * ✅ Genera un mapa con los conteos calculados en el servidor
     */
    public Map<String, Long> obtenerEstadisticasDashboard(String idClub) {
        Map<String, Long> stats = new HashMap<>();

        // Conteo de competidores y robots usando tus métodos personalizados
        stats.put("totalCompetidores", usuarioRepository.contarUsuariosPorClub(idClub));
        stats.put("totalRobots", robotRepository.contarRobotsPorClub(idClub));

        // ✅ Uso del repositorio inyectado para obtener el total de códigos
        // Usamos el método countByClubIdClub que definimos para el repositorio
        long total = codigoRegistroCompetidorRepository.countByClubIdClub(idClub);
        stats.put("totalCodigos", total);

        return stats;
    }
}

