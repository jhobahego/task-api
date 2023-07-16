package com.jhobadev.taskapi.controlador;

import com.jhobadev.taskapi.persistencia.entidad.Tarea;
import com.jhobadev.taskapi.servicio.TareaServicio;
import com.jhobadev.taskapi.servicio.dto.TareaInDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/tareas")
public class TareaControlador {

    private TareaServicio tareaServicio;

    public TareaControlador(TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
    }

    @GetMapping
    public List<Tarea> obtenerTodasLasTareas() {
        return this.tareaServicio.obtenerTodasLasTareas();
    }

    @PostMapping()
    public ResponseEntity<Tarea> crearTarea(@ModelAttribute TareaInDTO tareaInDTO) throws URISyntaxException {
        Tarea tareaCreada = this.tareaServicio.crearTarea(tareaInDTO);
        return ResponseEntity.created(new URI("/api/tareas"+tareaCreada.getId())).body(tareaCreada);
    }

    @PatchMapping("/completar/{id}")
    public ResponseEntity<?> completarTarea(@PathVariable("id") Long id) {
        this.tareaServicio.completarTarea(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable("id") Long id) {
        this.tareaServicio.eliminarTarea(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/eliminar-completadas")
    public ResponseEntity<Void> eliminarCompletadas() {
        this.tareaServicio.eliminarTareasCompletadas();

        return ResponseEntity.noContent().build();
    }
}
