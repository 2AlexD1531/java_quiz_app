package quizApp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import quizApp.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@ActiveProfiles("test")
class QuizResultRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Quiz quiz1;
    private Quiz quiz2;
    private Question question1;
    private Question question2;
    private QuizResult result1;
    private QuizResult result2;
    private QuizResult result3;

    @BeforeEach
    void setUp() {
        // Очистка базы данных перед каждым тестом
        quizResultRepository.deleteAll();
        userRepository.deleteAll();

        // Создание тестовых пользователей
        user1 = new User();
        user1.setUsername("test_user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password123");
        user1.setRole(Role.ROLE_USER);

        user2 = new User();
        user2.setUsername("test_user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password456");
        user2.setRole(Role.ROLE_USER);

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // Создание тестовых вопросов
        question1 = new Question();
        question1.setText("What is Java?");
        question1.setType(QuestionType.THEORY);
        question1.setOptions(List.of("Programming Language", "Coffee", "Island", "Car"));
        question1.setCorrectAnswer("A");
        question1.setExplanation("Java is a programming language");
        question1.setDifficulty("JUNIOR");
        question1.setTags(new HashSet<>(List.of("Java", "Basics")));

        question2 = new Question();
        question2.setText("What is Spring?");
        question2.setType(QuestionType.THEORY);
        question2.setOptions(List.of("Framework", "Season", "Water", "Jump"));
        question2.setCorrectAnswer("A");
        question2.setExplanation("Spring is a framework");
        question2.setDifficulty("JUNIOR");
        question2.setTags(new HashSet<>(List.of("Spring", "Framework")));

        entityManager.persist(question1);
        entityManager.persist(question2);
        entityManager.flush();

        // Создание тестовых квизов
        quiz1 = new Quiz();
        quiz1.setTitle("Java Basics Quiz");
        quiz1.setDescription("Test your Java knowledge");
        quiz1.setQuestions(List.of(question1, question2));
        quiz1.setTags(new HashSet<>(List.of("Java", "Basics")));
        quiz1.setDifficulty("JUNIOR");
        quiz1.setTimeLimit(30);

        quiz2 = new Quiz();
        quiz2.setTitle("Spring Framework Quiz");
        quiz2.setDescription("Test your Spring knowledge");
        quiz2.setQuestions(List.of(question2));
        quiz2.setTags(new HashSet<>(List.of("Spring")));
        quiz2.setDifficulty("MIDDLE");
        quiz2.setTimeLimit(20);

        entityManager.persist(quiz1);
        entityManager.persist(quiz2);
        entityManager.flush();

        // Создание userAnswers
        Map<Question, String> userAnswers1 = new HashMap<>();
        userAnswers1.put(question1, "A");
        userAnswers1.put(question2, "A");

        Map<Question, String> userAnswers2 = new HashMap<>();
        userAnswers2.put(question1, "B");
        userAnswers2.put(question2, "A");

        Map<Question, String> userAnswers3 = new HashMap<>();
        userAnswers3.put(question2, "A");

        // Создание тестовых результатов
        result1 = new QuizResult();
        result1.setQuiz(quiz1);
        result1.setScore(8);
        result1.setTotalQuestions(10);
        result1.setAttemptNumber(1);
        result1.setUserAnswers(userAnswers1);
        result1.setCompletedAt(LocalDateTime.now().minusHours(2));
        setUserViaReflection(result1, user1);

        result2 = new QuizResult();
        result2.setQuiz(quiz2);
        result2.setScore(5);
        result2.setTotalQuestions(5);
        result2.setAttemptNumber(1);
        result2.setUserAnswers(userAnswers2);
        result2.setCompletedAt(LocalDateTime.now().minusHours(1));
        setUserViaReflection(result2, user1);

        result3 = new QuizResult();
        result3.setQuiz(quiz1);
        result3.setScore(7);
        result3.setTotalQuestions(10);
        result3.setAttemptNumber(1);
        result3.setUserAnswers(userAnswers3);
        result3.setCompletedAt(LocalDateTime.now());
        setUserViaReflection(result3, user2);

        entityManager.persist(result1);
        entityManager.persist(result2);
        entityManager.persist(result3);
        entityManager.flush();
    }

    private void setUserViaReflection(QuizResult quizResult, User user) {
        try {
            var userField = QuizResult.class.getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(quizResult, user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user via reflection", e);
        }
    }

    @Test
    void whenFindByUser_thenReturnUserResults() {
        // When
        List<QuizResult> userResults = quizResultRepository.findByUser(user1);

        // Then
        assertThat(userResults).hasSize(2);
        assertThat(userResults).extracting(QuizResult::getUser)
                .allMatch(user -> user.getId().equals(user1.getId()));
    }

    @Test
    void whenFindByUserWithNoResults_thenReturnEmptyList() {
        // Given
        User newUser = new User();
        newUser.setUsername("new_user");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");
        entityManager.persistAndFlush(newUser);

        // When
        List<QuizResult> results = quizResultRepository.findByUser(newUser);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void whenFindByIdAndUserId_thenReturnResult() {
        // When
        Optional<QuizResult> found = quizResultRepository.findByIdAndUserId(result1.getId(), user1.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualTo(8);
        assertThat(found.get().getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    void whenFindByIdAndUserIdWithWrongUser_thenReturnEmpty() {
        // When
        Optional<QuizResult> found = quizResultRepository.findByIdAndUserId(result1.getId(), user2.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void whenFindMostActiveUsers_thenReturnUserActivity() {
        // When
        List<Object[]> activeUsers = quizResultRepository.findMostActiveUsers();

        // Then
        assertThat(activeUsers).hasSize(2);

        // user1 has 2 results, user2 has 1 result
        assertThat(activeUsers.get(0)).satisfies(item -> {
            assertThat(item[0]).isEqualTo("test_user1");
            assertThat(item[1]).isEqualTo(2L);
        });

        assertThat(activeUsers.get(1)).satisfies(item -> {
            assertThat(item[0]).isEqualTo("test_user2");
            assertThat(item[1]).isEqualTo(1L);
        });
    }

    @Test
    void whenFindAverageScore_thenReturnCorrectAverage() {
        // When
        Double averageScore = quizResultRepository.findAverageScore();

        // Then
        // (8 + 5 + 7) / 3 = 6.666...
        assertThat(averageScore).isCloseTo(6.666, within(0.01));
    }

    @Test
    void whenCountByMinScore_thenReturnCorrectCount() {
        // When
        Long countAbove6 = quizResultRepository.countByMinScore(6);
        Long countAbove8 = quizResultRepository.countByMinScore(8);

        // Then
        assertThat(countAbove6).isEqualTo(2); // result1 (8) and result3 (7)
        assertThat(countAbove8).isEqualTo(1); // only result1 (8)
    }

    @Test
    void whenFindByOrderByCompletedAtDesc_thenReturnResultsInCorrectOrder() {
        // When
        List<QuizResult> results = quizResultRepository.findByOrderByCompletedAtDesc();

        // Then
        assertThat(results).hasSize(3);
        // result3 was created last (now), then result2 (now-1h), then result1 (now-2h)
        assertThat(results.get(0).getId()).isEqualTo(result3.getId());
        assertThat(results.get(1).getId()).isEqualTo(result2.getId());
        assertThat(results.get(2).getId()).isEqualTo(result1.getId());
    }

    @Test
    void whenFindAll_thenReturnAllResults() {
        // When
        List<QuizResult> allResults = quizResultRepository.findAll();

        // Then
        assertThat(allResults).hasSize(3);
    }
}