package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.TipoEncuentro;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearEncuentrosDTO {

    @NotNull
    private String idCategoriaTorneo;

    @NotNull
    private TipoEncuentro tipoEncuentro; // 🔥 ENUM, no String

    @NotNull
    private String idJuez;

    @NotNull
    private String idColiseo;
}


