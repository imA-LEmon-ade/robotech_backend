package com.robotech.robotech_backend.model.entity;


import com.robotech.robotech_backend.model.enums.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Coliseo {
    @Id
    private String idColiseo;
    private String nombre;
    private String ubicacion;

    private String imagenUrl; // NUEVO
}



