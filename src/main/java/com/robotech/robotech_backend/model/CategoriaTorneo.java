package com.robotech.robotech_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias_torneo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaTorneo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_categoria_torneo")
    private String idCategoriaTorneo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_torneo", referencedColumnName = "id_torneo", nullable = false)
    @JsonIgnore
    private Torneo torneo;

    @OneToMany(mappedBy = "categoriaTorneo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default // ✅ Asegura que el Builder no deje la lista como null
    private List<Encuentro> encuentros = new ArrayList<>();

    @OneToMany(mappedBy = "categoriaTorneo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default // ✅ Asegura que el Builder no deje la lista como null
    private List<InscripcionTorneo> inscripciones = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaCompetencia categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalidadCategoria modalidad;

    private Integer maxParticipantes;
    private Integer maxEquipos;
    private Integer maxIntegrantesEquipo;
    private String descripcion;

    @Column(nullable = false)
    private Boolean inscripcionesCerradas = false;

    @PrePersist
    public void prePersist() {
        if (inscripcionesCerradas == null) {
            inscripcionesCerradas = false;
        }
    }
}