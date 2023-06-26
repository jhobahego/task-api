package com.jhobadev.taskapi.servicio;

import com.jhobadev.taskapi.mapper.TareaInDTOaTarea;
import com.jhobadev.taskapi.persistencia.entidad.Tarea;
import com.jhobadev.taskapi.persistencia.repositorio.TareaRepositorio;
import com.jhobadev.taskapi.servicio.dto.TareaInDTO;
import org.springframework.stereotype.Service;

@Service
public class TareaServicio {

    private TareaRepositorio tareaRepositorio;
    private TareaInDTOaTarea tareaInDTOaTarea;

    public TareaServicio(TareaRepositorio tareaRepositorio, TareaInDTOaTarea tareaInDTOaTarea) {
        this.tareaRepositorio = tareaRepositorio;
        this.tareaInDTOaTarea = tareaInDTOaTarea;
    }

    public Tarea crearTarea(TareaInDTO tareaInDTO) {
        Tarea tarea = tareaInDTOaTarea.map(tareaInDTO);
        return tareaRepositorio.save(tarea);
    }
}
