package com.jhobadev.taskapi.servicio;

import com.jhobadev.taskapi.mapper.TareaInDTOaTarea;
import com.jhobadev.taskapi.persistencia.entidad.Tarea;
import com.jhobadev.taskapi.persistencia.repositorio.TareaRepositorio;
import com.jhobadev.taskapi.servicio.dto.TareaInDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TareaServicio {

    private TareaRepositorio tareaRepositorio;
    private TareaInDTOaTarea tareaInDTOaTarea;

    public TareaServicio(TareaRepositorio tareaRepositorio, TareaInDTOaTarea tareaInDTOaTarea) {
        this.tareaRepositorio = tareaRepositorio;
        this.tareaInDTOaTarea = tareaInDTOaTarea;
    }

    public List<Tarea> obtenerTodasLasTareas() {
        return this.tareaRepositorio.findAll();
    }

    public Tarea crearTarea(TareaInDTO tareaInDTO) {
        Tarea tarea = tareaInDTOaTarea.map(tareaInDTO);
        return this.tareaRepositorio.save(tarea);
    }

    @Transactional
    public void completarTarea(Long id) {
        this.tareaRepositorio.completarTarea(id);
    }
}
