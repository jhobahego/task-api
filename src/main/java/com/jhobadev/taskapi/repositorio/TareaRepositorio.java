package com.jhobadev.taskapi.repositorio;

import com.jhobadev.taskapi.modelos.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TareaRepositorio extends JpaRepository<Tarea, Long> {

    @Modifying
    @Query(value = "UPDATE TAREA SET completed=:completed WHERE ID=:id", nativeQuery = true)
    void completarTarea(@Param("completed") boolean completed, @Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM TAREA WHERE completed=true", nativeQuery = true)
    void eliminarTareasCompletadas();

    Optional<Tarea> findByTitle(String title);
}
