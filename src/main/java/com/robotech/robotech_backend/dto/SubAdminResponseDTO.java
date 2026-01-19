package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoSubAdmin;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubAdminResponseDTO {

    private String idSubadmin;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private EstadoSubAdmin estado;
}

