package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, String> {

    Optional<Club> findByUsuario_IdUsuario(String idUsuario);

    @Query("""
        SELECT c
        FROM Club c
        JOIN FETCH c.usuario
        WHERE c.usuario.idUsuario = :idUsuario
    """)
    Optional<Club> findByUsuarioIdUsuarioFetch(@Param("idUsuario") String idUsuario);

    List<Club> findByNombreContainingIgnoreCase(String nombre);

    Optional<Club> findByUsuario(Usuario usuario);

    Optional<Club> findByCodigoClub(String codigoClub);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByCorreoContacto(String correoContacto);

    boolean existsByTelefonoContacto(String telefonoContacto);

    // ✅ ESTE ERA EL QUE FALTABA
    @Query("""
        SELECT c
        FROM Club c
        JOIN FETCH c.usuario
    """)
    List<Club> findAllWithUsuario();

    @Query(
        value = """
        SELECT c
        FROM Club c
        JOIN FETCH c.usuario u
        WHERE (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
        """,
        countQuery = """
        SELECT COUNT(c)
        FROM Club c
        JOIN c.usuario u
        WHERE (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
        """
    )
    Page<Club> buscarPorNombre(@Param("nombre") String nombre, Pageable pageable);
}


