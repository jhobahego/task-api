package com.jhobadev.taskapi.excepciones;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class TareaExcepciones extends RuntimeException{
    private String mensaje;
    private HttpStatus estatus;

    public TareaExcepciones(String mensaje, HttpStatus estatus) {
        super(mensaje);
        this.mensaje = mensaje;
        this.estatus = estatus;
    }
}
