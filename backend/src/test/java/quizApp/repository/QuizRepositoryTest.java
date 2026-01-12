package quizApp.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import quizApp.model.Question;
import quizApp.model.QuestionType;
import quizApp.model.Quiz;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class QuizRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void whenSaveQuiz_thenPersisted() {
        // Given
        Question question = new Question();
        question.setText("Test Question");
        question.setType(QuestionType.THEORY);
        question.setOptions(List.of("A", "B", "C", "D"));
        question.setCorrectAnswer("A");
        question.setExplanation("Explanation");
        question.setDifficulty("JUNIOR");
        question.setTags(Set.of("Java"));

        entityManager.persist(question);
        entityManager.flush();

        Quiz quiz = new Quiz();
        quiz.setTitle("Test Quiz");
        quiz.setDescription("Test Description");
        quiz.setQuestions(List.of(question));
        quiz.setTags(Set.of("Test"));
        quiz.setDifficulty("JUNIOR");
        quiz.setTimeLimit(30);

        // When
        Quiz saved = quizRepository.save(quiz);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(quizRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void whenFindAll_thenReturnAllQuizzes() {
        // Given
        Quiz quiz = new Quiz();
        quiz.setTitle("Test Quiz");
        quiz.setDescription("Test Description");
        quiz.setQuestions(List.of());
        quiz.setTags(Set.of("Test"));
        quiz.setDifficulty("JUNIOR");
        quiz.setTimeLimit(30);

        entityManager.persist(quiz);
        entityManager.flush();

        // When
        List<Quiz> quizzes = quizRepository.findAll();

        // Then
        assertThat(quizzes).hasSize(1);
        assertThat(quizzes.get(0).getTitle()).isEqualTo("Test Quiz");
    }
}