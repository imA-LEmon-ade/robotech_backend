package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CrearJuezDTO;
import com.robotech.robotech_backend.service.SubAdminJuezService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subadmin/jueces")
@RequiredArgsConstructor
public class SubAdminJuezController {

    private final SubAdminJuezService service;

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody @Valid CrearJuezDTO dto) {
        service.crearJuez(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}



