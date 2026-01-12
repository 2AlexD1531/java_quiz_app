package quizApp.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import quizApp.model.Question;
import quizApp.model.QuestionType;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class QuestionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void whenFindByTagsContaining_thenReturnQuestions() {
        // Given
        Question question = new Question();
        question.setText("What is Java?");
        question.setType(QuestionType.THEORY);
        question.setOptions(List.of("Language", "Coffee", "Island", "Car"));
        question.setCorrectAnswer("A");
        question.setExplanation("Java is programming language");
        question.setDifficulty("JUNIOR");
        question.setTags(Set.of("Java", "OOP"));

        entityManager.persist(question);
        entityManager.flush();

        // When
        List<Question> questions = questionRepository.findByTagsContaining("Java");

        // Then
        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).getText()).isEqualTo("What is Java?");
    }

    @Test
    void whenFindAllDistinctTags_thenReturnAllTags() {
        // Given
        Question question1 = new Question();
        question1.setText("Question 1");
        question1.setType(QuestionType.THEORY);
        question1.setOptions(List.of("A", "B", "C", "D"));
        question1.setCorrectAnswer("A");
        question1.setExplanation("Explanation 1");
        question1.setDifficulty("JUNIOR");
        question1.setTags(Set.of("Java", "OOP"));

        Question question2 = new Question();
        question2.setText("Question 2");
        question2.setType(QuestionType.CODE);
        question2.setOptions(List.of("A", "B", "C", "D"));
        question2.setCorrectAnswer("B");
        question2.setExplanation("Explanation 2");
        question2.setDifficulty("MIDDLE");
        question2.setTags(Set.of("Spring", "Framework"));

        entityManager.persist(question1);
        entityManager.persist(question2);
        entityManager.flush();

        // When
        List<String> tags = questionRepository.findAllDistinctTags();

        // Then
        assertThat(tags).hasSize(4);
        assertThat(tags).contains("Java", "OOP", "Spring", "Framework");
    }
}