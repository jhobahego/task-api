package com.jhobadev.taskapi.repositorio;

import com.jhobadev.taskapi.modelos.Tarea;
import com.jhobadev.taskapi.soporte.PostgresContainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository slice tests backed by the real PostgreSQL container.
 * Native modifying queries bypass the persistence context, so the tests
 * flush and clear around them before re-reading the database state.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TareaRepositorioTest extends PostgresContainerBase {

	@Autowired
	private TareaRepositorio tareaRepositorio;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void saveAssignsGeneratedId() {
		Tarea saved = tareaRepositorio.saveAndFlush(task("Comprar leche", false));

		assertThat(saved.getId()).isNotNull();
	}

	@Test
	void findByTitleReturnsTaskWhenPresent() {
		Tarea saved = tareaRepositorio.saveAndFlush(task("Comprar leche", false));
		entityManager.flush();
		entityManager.clear();

		Optional<Tarea> found = tareaRepositorio.findByTitle("Comprar leche");

		assertThat(found).isPresent();
		assertThat(found.get().getId()).isEqualTo(saved.getId());
		assertThat(found.get().getTitle()).isEqualTo("Comprar leche");
	}

	@Test
	void findByTitleReturnsEmptyWhenAbsent() {
		assertThat(tareaRepositorio.findByTitle("No existe")).isEmpty();
	}

	@Test
	void completarTareaUpdatesFlagToTrue() {
		Tarea saved = tareaRepositorio.saveAndFlush(task("Comprar leche", false));
		entityManager.flush();
		entityManager.clear();

		tareaRepositorio.completarTarea(true, saved.getId());
		entityManager.flush();
		entityManager.clear();

		Tarea reloaded = tareaRepositorio.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.isCompleted()).isTrue();
	}

	@Test
	void completarTareaUpdatesFlagToFalse() {
		Tarea saved = tareaRepositorio.saveAndFlush(task("Comprar leche", true));
		entityManager.flush();
		entityManager.clear();

		tareaRepositorio.completarTarea(false, saved.getId());
		entityManager.flush();
		entityManager.clear();

		Tarea reloaded = tareaRepositorio.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.isCompleted()).isFalse();
	}

	@Test
	void eliminarTareasCompletadasDeletesOnlyCompleted() {
		Tarea completedOne = tareaRepositorio.save(task("Comprar leche", true));
		Tarea completedTwo = tareaRepositorio.save(task("Pagar luz", true));
		Tarea pending = tareaRepositorio.save(task("Llamar al medico", false));
		entityManager.flush();
		entityManager.clear();

		assertThat(completedOne.getId()).isNotNull();
		assertThat(completedTwo.getId()).isNotNull();

		tareaRepositorio.eliminarTareasCompletadas();
		entityManager.flush();
		entityManager.clear();

		List<Tarea> remaining = tareaRepositorio.findAll();
		assertThat(remaining).hasSize(1);
		assertThat(remaining.get(0).getId()).isEqualTo(pending.getId());
		assertThat(remaining.get(0).getTitle()).isEqualTo("Llamar al medico");
		assertThat(remaining.get(0).isCompleted()).isFalse();
	}

	@Test
	void duplicateTitleViolatesUniqueConstraint() {
		tareaRepositorio.saveAndFlush(task("Comprar leche", false));

		assertThatThrownBy(() -> tareaRepositorio.saveAndFlush(task("Comprar leche", false)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private static Tarea task(String title, boolean completed) {
		Tarea task = new Tarea();
		task.setTitle(title);
		task.setCompleted(completed);
		return task;
	}
}
