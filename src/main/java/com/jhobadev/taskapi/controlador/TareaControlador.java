package com.jhobadev.taskapi.controlador;

import com.jhobadev.taskapi.persistencia.entidad.Tarea;
import com.jhobadev.taskapi.servicio.TareaServicio;
import com.jhobadev.taskapi.servicio.dto.TareaInDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tareas")
public class TareaControlador {

    private TareaServicio tareaServicio;

    public TareaControlador(TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Tarea crearTarea(@ModelAttribute TareaInDTO tareaInDTO) {
        return this.tareaServicio.crearTarea(tareaInDTO);
    }
}
