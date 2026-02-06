package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.EstadoClub;
import lombok.Data;

@Data
public class EditarClubDTO {
    private String nombre;
    private String correoContacto;
    private String telefonoContacto;
    private String direccionFiscal;
    private EstadoClub estado;
}


