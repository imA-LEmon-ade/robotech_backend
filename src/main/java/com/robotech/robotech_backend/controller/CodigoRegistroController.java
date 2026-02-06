package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.entity.CodigoRegistroCompetidor;
import com.robotech.robotech_backend.repository.CodigoRegistroCompetidorRepository;
import com.robotech.robotech_backend.service.CodigoRegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Map;

@RestController
@RequestMapping("/api/codigos")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url}")
public class CodigoRegistroController {

    private final CodigoRegistroService codigoService;

    @Autowired
    private CodigoRegistroCompetidorRepository codigoRepo;

    // 📌 Generar código personalizado
    @PostMapping("/{idClub}/generar")
    public CodigoRegistroCompetidor generarCodigo(
            @PathVariable String idClub,
            @RequestBody Map<String, Integer> config
    ) {
        int horasExpiracion = config.get("horasExpiracion");
        int limiteUso = config.get("limiteUso");

        return codigoService.generarCodigoParaClub(idClub, horasExpiracion, limiteUso);
    }

    // 📌 Validar código
    @GetMapping("/validar/{codigo}")
    public CodigoRegistroCompetidor validarCodigo(@PathVariable String codigo) {
        return codigoService.validarCodigo(codigo);
    }

    @GetMapping("/club/{idClub}")
    public List<CodigoRegistroCompetidor> listarCodigosPorClub(@PathVariable String idClub) {
        return codigoService.listarCodigosPorClub(idClub);
    }

}


