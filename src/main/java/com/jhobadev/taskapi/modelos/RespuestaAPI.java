package com.jhobadev.taskapi.modelos;

import lombok.Data;

@Data
public class RespuestaAPI {

    Tarea tarea;

    String mensaje;

    boolean exito;
}
