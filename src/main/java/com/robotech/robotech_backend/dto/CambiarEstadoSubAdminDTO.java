package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.EstadoSubAdmin;
import lombok.Data;

@Data
public class CambiarEstadoSubAdminDTO {
    private EstadoSubAdmin estado;
}


