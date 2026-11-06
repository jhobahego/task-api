package com.jhobadev.taskapi.servicio;

import com.jhobadev.taskapi.dtos.TareaInDTO;
import com.jhobadev.taskapi.excepciones.TareaExcepciones;
import com.jhobadev.taskapi.mapper.TareaInDTOaTarea;
import com.jhobadev.taskapi.modelos.Tarea;
import com.jhobadev.taskapi.repositorio.TareaRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Characterization tests for the business rules of {@link TareaServicio}.
 * Pure unit tests: no Spring context, the repository and the mapper are mocked.
 */
@ExtendWith(MockitoExtension.class)
class TareaServicioTest {

	@Mock
	private TareaRepositorio tareaRepositorio;

	@Mock
	private TareaInDTOaTarea tareaInDTOaTarea;

	@InjectMocks
	private TareaServicio tareaServicio;

	@Test
	void returnsAllTasksFromRepository() {
		List<Tarea> tasks = List.of(task(1L, "Comprar leche", false));
		when(tareaRepositorio.findAll()).thenReturn(tasks);

		List<Tarea> result = tareaServicio.obtenerTodasLasTareas();

		assertThat(result).isSameAs(tasks);
	}

	@Test
	void createsTaskWhenTitleIsValidAndUnique() {
		TareaInDTO input = taskInput("Comprar leche");
		Tarea mapped = task(null, "Comprar leche", false);
		Tarea saved = task(1L, "Comprar leche", false);
		when(tareaRepositorio.findByTitle("Comprar leche")).thenReturn(Optional.empty());
		when(tareaInDTOaTarea.map(input)).thenReturn(mapped);
		when(tareaRepositorio.save(mapped)).thenReturn(saved);

		Tarea result = tareaServicio.crearTarea(input);

		assertThat(result).isSameAs(saved);
		verify(tareaRepositorio).save(mapped);
	}

	@Test
	void acceptsTitleWithExactlyFourCharacters() {
		TareaInDTO input = taskInput("Pago");
		Tarea mapped = task(null, "Pago", false);
		Tarea saved = task(2L, "Pago", false);
		when(tareaRepositorio.findByTitle("Pago")).thenReturn(Optional.empty());
		when(tareaInDTOaTarea.map(input)).thenReturn(mapped);
		when(tareaRepositorio.save(mapped)).thenReturn(saved);

		Tarea result = tareaServicio.crearTarea(input);

		assertThat(result).isSameAs(saved);
		verify(tareaRepositorio).save(mapped);
	}

	@Test
	void rejectsDuplicateTitleWithBadRequest() {
		when(tareaRepositorio.findByTitle("Comprar leche"))
				.thenReturn(Optional.of(task(1L, "Comprar leche", false)));

		assertThatThrownBy(() -> tareaServicio.crearTarea(taskInput("Comprar leche")))
				.isInstanceOfSatisfying(TareaExcepciones.class, exception -> {
					assertThat(exception.getEstatus()).isEqualTo(HttpStatus.BAD_REQUEST);
					assertThat(exception.getMensaje()).isEqualTo("Tarea ya existente, corrige el titulo");
				});

		verify(tareaRepositorio, never()).save(any(Tarea.class));
	}

	@Test
	void rejectsTitleShorterThanFourCharactersWithBadRequest() {
		when(tareaRepositorio.findByTitle("Sol")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> tareaServicio.crearTarea(taskInput("Sol")))
				.isInstanceOfSatisfying(TareaExcepciones.class, exception -> {
					assertThat(exception.getEstatus()).isEqualTo(HttpStatus.BAD_REQUEST);
					assertThat(exception.getMensaje()).isEqualTo("La tarea debe tener minimo 4 caracteres");
				});

		verify(tareaRepositorio, never()).save(any(Tarea.class));
	}

	@Test
	void crearTareaWithNullTitleThrowsBadRequest() {
		TareaInDTO input = taskInput(null);
		when(tareaRepositorio.findByTitle(isNull())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> tareaServicio.crearTarea(input))
				.isInstanceOfSatisfying(TareaExcepciones.class, exception -> {
					assertThat(exception.getEstatus()).isEqualTo(HttpStatus.BAD_REQUEST);
					assertThat(exception.getMensaje()).isEqualTo("La tarea debe tener minimo 4 caracteres");
				});

		verify(tareaRepositorio, never()).save(any(Tarea.class));
		verify(tareaInDTOaTarea, never()).map(any(TareaInDTO.class));
	}

	@Test
	void marksIncompleteTaskAsCompleted() {
		when(tareaRepositorio.findById(1L)).thenReturn(Optional.of(task(1L, "Comprar leche", false)));

		tareaServicio.completarTarea(1L);

		verify(tareaRepositorio).completarTarea(true, 1L);
	}

	@Test
	void marksCompletedTaskAsIncomplete() {
		when(tareaRepositorio.findById(1L)).thenReturn(Optional.of(task(1L, "Comprar leche", true)));

		tareaServicio.completarTarea(1L);

		verify(tareaRepositorio).completarTarea(false, 1L);
	}

	@Test
	void rejectsCompletingMissingTaskWithNotFound() {
		when(tareaRepositorio.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> tareaServicio.completarTarea(99L))
				.isInstanceOfSatisfying(TareaExcepciones.class, exception -> {
					assertThat(exception.getEstatus()).isEqualTo(HttpStatus.NOT_FOUND);
					assertThat(exception.getMensaje()).isEqualTo("Tarea no encontrada");
				});

		verify(tareaRepositorio, never()).completarTarea(anyBoolean(), anyLong());
	}

	@Test
	void deletesExistingTask() {
		when(tareaRepositorio.findById(1L)).thenReturn(Optional.of(task(1L, "Comprar leche", false)));

		tareaServicio.eliminarTarea(1L);

		verify(tareaRepositorio).deleteById(1L);
	}

	@Test
	void rejectsDeletingMissingTaskWithNotFound() {
		when(tareaRepositorio.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> tareaServicio.eliminarTarea(99L))
				.isInstanceOfSatisfying(TareaExcepciones.class, exception -> {
					assertThat(exception.getEstatus()).isEqualTo(HttpStatus.NOT_FOUND);
					assertThat(exception.getMensaje()).isEqualTo("Tarea no encontrada");
				});

		verify(tareaRepositorio, never()).deleteById(anyLong());
	}

	@Test
	void delegatesCompletedTaskDeletionToRepository() {
		tareaServicio.eliminarTareasCompletadas();

		verify(tareaRepositorio).eliminarTareasCompletadas();
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
