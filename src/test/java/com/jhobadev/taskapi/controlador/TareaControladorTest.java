package com.jhobadev.taskapi.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhobadev.taskapi.dtos.TareaInDTO;
import com.jhobadev.taskapi.excepciones.TareaExcepciones;
import com.jhobadev.taskapi.modelos.Tarea;
import com.jhobadev.taskapi.servicio.TareaServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice tests for {@link TareaControlador}.
 * The service is mocked, so no database or container is started.
 */
@WebMvcTest(TareaControlador.class)
class TareaControladorTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TareaServicio tareaServicio;

	@Test
	void obtenerTodasLasTareasReturnsTaskList() throws Exception {
		when(tareaServicio.obtenerTodasLasTareas()).thenReturn(List.of(
				task(1L, "Comprar leche", false),
				task(2L, "Pagar luz", true)));

		mockMvc.perform(get("/api/tareas"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].title").value("Comprar leche"))
				.andExpect(jsonPath("$[1].title").value("Pagar luz"));
	}

	@Test
	void crearTareaReturnsCreatedWithLocationAndBody() throws Exception {
		TareaInDTO input = taskInput("Comprar leche");
		when(tareaServicio.crearTarea(any(TareaInDTO.class)))
				.thenReturn(task(5L, "Comprar leche", false));

		mockMvc.perform(post("/api/tareas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isCreated())
				.andExpect(header().string(HttpHeaders.LOCATION, "/api/tareas/5"))
				.andExpect(jsonPath("$.exito").value(true))
				.andExpect(jsonPath("$.mensaje").value("Tarea creada correctamente"))
				.andExpect(jsonPath("$.tarea.id").value(5));
	}

	@Test
	void crearTareaWithNullTitleReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/tareas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":null}"))
				.andExpect(status().isBadRequest());

		verify(tareaServicio, never()).crearTarea(any(TareaInDTO.class));
	}

	@Test
	void crearTareaWithBlankTitleReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/tareas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"   \"}"))
				.andExpect(status().isBadRequest());

		verify(tareaServicio, never()).crearTarea(any(TareaInDTO.class));
	}

	@Test
	void crearTareaWithShortTitleReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/tareas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"abc\"}"))
				.andExpect(status().isBadRequest());

		verify(tareaServicio, never()).crearTarea(any(TareaInDTO.class));
	}

	@Test
	void completarTareaReturnsNoContent() throws Exception {
		mockMvc.perform(patch("/api/tareas/completar/1"))
				.andExpect(status().isNoContent());

		verify(tareaServicio).completarTarea(1L);
	}

	@Test
	void eliminarTareaReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/api/tareas/eliminar/1"))
				.andExpect(status().isNoContent());

		verify(tareaServicio).eliminarTarea(1L);
	}

	@Test
	void eliminarTareasCompletadasReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/api/tareas/eliminar-completadas"))
				.andExpect(status().isNoContent());

		verify(tareaServicio).eliminarTareasCompletadas();
	}

	@Test
	void completarMissingTaskReturnsNotFoundWithMessage() throws Exception {
		doThrow(new TareaExcepciones("Tarea no encontrada", HttpStatus.NOT_FOUND))
				.when(tareaServicio).completarTarea(99L);

		mockMvc.perform(patch("/api/tareas/completar/99"))
				.andExpect(status().isNotFound())
				.andExpect(content().string("Tarea no encontrada"));
	}

	private static TareaInDTO taskInput(String title) {
		TareaInDTO input = new TareaInDTO();
		input.setTitle(title);
		return input;
	}

	private static Tarea task(Long id, String title, boolean completed) {
		Tarea task = new Tarea();
		task.setId(id);
		task.setTitle(title);
		task.setCompleted(completed);
		return task;
	}
}
