package com.jhobadev.taskapi.servicio;

import com.jhobadev.taskapi.excepciones.TareaExcepciones;
import com.jhobadev.taskapi.mapper.TareaInDTOaTarea;
import com.jhobadev.taskapi.modelos.Tarea;
import com.jhobadev.taskapi.repositorio.TareaRepositorio;
import com.jhobadev.taskapi.dtos.TareaInDTO;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
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
        Optional<Tarea> tareaOptional = tareaRepositorio.findByTitle(tareaInDTO.getTitle());
        if (tareaOptional.isPresent()) {
            throw new TareaExcepciones("Tarea ya existente, corrige el titulo", HttpStatus.BAD_REQUEST);
        }

        if (tareaInDTO.getTitle().length() < 4) {
            throw new TareaExcepciones("La tarea debe tener minimo 4 caracteres", HttpStatus.BAD_REQUEST);
        }

        Tarea tarea = tareaInDTOaTarea.map(tareaInDTO);
        return this.tareaRepositorio.save(tarea);
    }

    @Transactional
    public void completarTarea(Long id) {
        Optional<Tarea> tareaOptional = this.tareaRepositorio.findById(id);
        if (tareaOptional.isEmpty()) {
            throw new TareaExcepciones("Tarea no encontrada", HttpStatus.NOT_FOUND);
        }

        Tarea tarea = tareaOptional.get();
        boolean completed = !tarea.isCompleted();
        this.tareaRepositorio.completarTarea(completed, id);
    }

    public void eliminarTarea(Long id) {
        Optional<Tarea> tareaOptional = this.tareaRepositorio.findById(id);
        if (tareaOptional.isEmpty()) {
            throw new TareaExcepciones("Tarea no encontrada", HttpStatus.NOT_FOUND);
        }

        this.tareaRepositorio.deleteById(id);
    }

    @Transactional
    public void eliminarTareasCompletadas() {
        this.tareaRepositorio.eliminarTareasCompletadas();
    }
}
