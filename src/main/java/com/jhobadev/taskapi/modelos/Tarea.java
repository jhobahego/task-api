package com.jhobadev.taskapi.modelos;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tarea")
public class Tarea {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String title;

    private boolean completed;
}
