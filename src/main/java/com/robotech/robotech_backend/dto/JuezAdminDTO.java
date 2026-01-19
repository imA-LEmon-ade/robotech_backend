package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoValidacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JuezAdminDTO {

    private String idJuez;
    private String licencia;
    private EstadoValidacion estadoValidacion;

    private UsuarioDTO usuario;

}

