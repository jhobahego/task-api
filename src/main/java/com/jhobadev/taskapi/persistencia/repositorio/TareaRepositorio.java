package com.jhobadev.taskapi.persistencia.repositorio;

import com.jhobadev.taskapi.persistencia.entidad.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TareaRepositorio extends JpaRepository<Tarea, Long> {

    @Modifying
    @Query(value = "UPDATE TAREA SET completed=true WHERE ID=:id", nativeQuery = true)
    void completarTarea(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM TAREA WHERE completed=true", nativeQuery = true)
    void eliminarTareasCompletadas();
}
