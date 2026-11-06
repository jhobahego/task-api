package com.jhobadev.taskapi;

import com.jhobadev.taskapi.dtos.TareaInDTO;
import com.jhobadev.taskapi.modelos.RespuestaAPI;
import com.jhobadev.taskapi.modelos.Tarea;
import com.jhobadev.taskapi.repositorio.TareaRepositorio;
import com.jhobadev.taskapi.soporte.PostgresContainerBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests over HTTP backed by the real PostgreSQL container.
 * The shared container from {@link PostgresContainerBase} is reused.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskApiIntegrationTest extends PostgresContainerBase {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private TareaRepositorio tareaRepositorio;

	@BeforeEach
	void cleanDatabase() {
		tareaRepositorio.deleteAll();
	}

	@Test
	void createListCompleteAndDeleteCompletedFlow() {
		ResponseEntity<RespuestaAPI> created = restTemplate.postForEntity(
				"/api/tareas", taskInput("Comprar leche"), RespuestaAPI.class);

		assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(created.getBody()).isNotNull();
		assertThat(created.getBody().isExito()).isTrue();
		assertThat(created.getBody().getMensaje()).isEqualTo("Tarea creada correctamente");
		assertThat(created.getBody().getTarea()).isNotNull();

		Long id = created.getBody().getTarea().getId();
		assertThat(id).isNotNull();
		assertThat(created.getHeaders().getLocation()).isNotNull();
		assertThat(created.getHeaders().getLocation().getPath()).isEqualTo("/api/tareas/" + id);

		ResponseEntity<Tarea[]> afterCreate = restTemplate.getForEntity("/api/tareas", Tarea[].class);
		assertThat(afterCreate.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(afterCreate.getBody()).isNotNull();
		assertThat(afterCreate.getBody()).hasSize(1);
		assertThat(afterCreate.getBody()[0].getId()).isEqualTo(id);
		assertThat(afterCreate.getBody()[0].getTitle()).isEqualTo("Comprar leche");
		assertThat(afterCreate.getBody()[0].isCompleted()).isFalse();

		ResponseEntity<Void> completed = restTemplate.exchange(
				"/api/tareas/completar/" + id, HttpMethod.PATCH, emptyEntity(), Void.class);
		assertThat(completed.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<Tarea[]> afterComplete = restTemplate.getForEntity("/api/tareas", Tarea[].class);
		assertThat(afterComplete.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(afterComplete.getBody()).isNotNull();
		assertThat(afterComplete.getBody()).hasSize(1);
		assertThat(afterComplete.getBody()[0].isCompleted()).isTrue();

		ResponseEntity<Void> deleted = restTemplate.exchange(
				"/api/tareas/eliminar-completadas", HttpMethod.DELETE, emptyEntity(), Void.class);
		assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<Tarea[]> afterDelete = restTemplate.getForEntity("/api/tareas", Tarea[].class);
		assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(afterDelete.getBody()).isNotNull();
		assertThat(afterDelete.getBody()).isEmpty();
	}

	@Test
	void duplicateTitleReturns400() {
		ResponseEntity<RespuestaAPI> first = restTemplate.postForEntity(
				"/api/tareas", taskInput("Comprar leche"), RespuestaAPI.class);
		assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		ResponseEntity<String> duplicate = restTemplate.postForEntity(
				"/api/tareas", taskInput("Comprar leche"), String.class);

		assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(duplicate.getBody()).contains("Tarea ya existente, corrige el titulo");
	}

	@Test
	void completeMissingTaskReturns404() {
		ResponseEntity<String> response = restTemplate.exchange(
				"/api/tareas/completar/999999", HttpMethod.PATCH, emptyEntity(), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isEqualTo("Tarea no encontrada");
	}

	@Test
	void invalidTitleReturns400() {
		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/tareas", taskInput("abc"), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	private static HttpEntity<Void> emptyEntity() {
		return new HttpEntity<>(new HttpHeaders());
	}

	private static TareaInDTO taskInput(String title) {
		TareaInDTO input = new TareaInDTO();
		input.setTitle(title);
		return input;
	}
}
