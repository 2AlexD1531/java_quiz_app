package quizApp.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import quizApp.model.User;
import quizApp.model.Role;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
        assertThat(userRepository).isNotNull();
        assertThat(entityManager).isNotNull();
    }

    @Test
    void whenSaveUser_thenCanFindById() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.ROLE_USER);

        // When
        User saved = userRepository.save(user);

        // Then
        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void whenFindByUsername_thenReturnUser() {
        // Given
        User user = new User();
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setRole(Role.ROLE_USER);

        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByUsername("john_doe");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void whenFindByEmail_thenReturnUser() {
        // Given
        User user = new User();
        user.setUsername("jane_smith");
        user.setEmail("jane@example.com");
        user.setPassword("password456");
        user.setRole(Role.ROLE_USER);

        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByEmail("jane@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("jane_smith");
    }

    @Test
    void whenFindByNonExistingUsername_thenReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexisting");

        // Then
        assertThat(found).isEmpty();
    }
}