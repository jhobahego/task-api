package com.jhobadev.taskapi.mapper;

import com.jhobadev.taskapi.dtos.TareaInDTO;
import com.jhobadev.taskapi.modelos.Tarea;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization test for {@link TareaInDTOaTarea}. Pure unit test: no Spring context.
 */
class TareaInDTOaTareaTest {

	@Test
	void mapsTitleAndStartsIncompleteWithoutId() {
		TareaInDTO input = new TareaInDTO();
		input.setTitle("Comprar leche");

		Tarea task = new TareaInDTOaTarea().map(input);

		assertThat(task.getTitle()).isEqualTo("Comprar leche");
		assertThat(task.isCompleted()).isFalse();
		assertThat(task.getId()).isNull();
	}
}
