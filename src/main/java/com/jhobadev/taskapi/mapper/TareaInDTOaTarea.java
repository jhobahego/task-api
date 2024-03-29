package com.jhobadev.taskapi.mapper;

import com.jhobadev.taskapi.modelos.Tarea;
import com.jhobadev.taskapi.dtos.TareaInDTO;
import org.springframework.stereotype.Component;

@Component
public class TareaInDTOaTarea implements IMapper<TareaInDTO, Tarea>{

    @Override
    public Tarea map(TareaInDTO in) {
        Tarea tarea = new Tarea();
        tarea.setTitle(in.getTitle());
        tarea.setCompleted(false);
        return tarea;
    }
}
