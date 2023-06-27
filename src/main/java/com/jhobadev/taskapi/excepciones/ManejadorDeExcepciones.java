package com.jhobadev.taskapi.excepciones;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ManejadorDeExcepciones extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {TareaExcepciones.class})
    protected ResponseEntity<Object> manejador( TareaExcepciones ex, WebRequest solicitud) {
        String respuesta = ex.getMensaje();
        return handleExceptionInternal(ex, respuesta, new HttpHeaders(), ex.getEstatus(), solicitud);
    }
}
