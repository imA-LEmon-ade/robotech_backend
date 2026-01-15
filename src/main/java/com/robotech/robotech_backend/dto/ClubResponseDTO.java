package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoClub;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClubResponseDTO {
    private String idClub;
    private String codigoClub;

    private String nombre;
    private String correoContacto;
    private String telefonoContacto;
    private String direccionFiscal;
    private EstadoClub estado;


    private String correoPropietario;
}
