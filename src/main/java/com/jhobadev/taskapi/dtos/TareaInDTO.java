package com.jhobadev.taskapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TareaInDTO {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(min = 4, message = "La tarea debe tener minimo 4 caracteres")
    String title;
}
