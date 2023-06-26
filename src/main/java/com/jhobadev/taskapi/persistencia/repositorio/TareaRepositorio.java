package com.jhobadev.taskapi.persistencia.repositorio;

import com.jhobadev.taskapi.persistencia.entidad.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TareaRepositorio extends JpaRepository<Tarea, Long> {
}
