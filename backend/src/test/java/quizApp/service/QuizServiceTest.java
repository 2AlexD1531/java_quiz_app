package quizApp.service;

import org.junit.jupiter.api.BeforeEach;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import quizApp.model.Question;
import quizApp.model.Quiz;

import quizApp.model.QuizResult;
import quizApp.model.dto.QuestionDTO;
import quizApp.model.dto.QuizRequest;
import quizApp.model.dto.QuizResponse;
import quizApp.repository.QuestionRepository;
import quizApp.repository.QuizRepository;
import quizApp.repository.QuizResultRepository;
import quizApp.service.AIService;
import quizApp.service.QuizService;


import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuizResultRepository quizResultRepository;

    @Mock
    private AIService aiService;

    @InjectMocks
    private QuizService quizService;

    private QuizRequest quizRequest;
    private List<Question> sampleQuestions;
    private Quiz sampleQuiz;

    @BeforeEach
    void setUp() {
        // Setup quiz data
        quizRequest = new QuizRequest();
        quizRequest.setTags(Arrays.asList("Java", "OOP"));
        quizRequest.setDifficulty("JUNIOR");
        quizRequest.setQuestionCount(3);

        // Create sample questions
        sampleQuestions = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Question question = new Question();
            question.setId((long) i);
            question.setText("Question " + i);
            question.setOptions(Arrays.asList("A", "B", "C", "D"));
            question.setCorrectAnswer("A");
            question.setExplanation("Explanation " + i);
            question.setTags(Set.of("Java"));
            question.setDifficulty("JUNIOR");
            sampleQuestions.add(question);
        }

        // Create sample quiz
        sampleQuiz = new Quiz();
        sampleQuiz.setId(1L);
        sampleQuiz.setTitle("Тест: Java, OOP");
        sampleQuiz.setDescription("Автоматически сгенерированный тест");
        sampleQuiz.setQuestions(sampleQuestions);
        sampleQuiz.setTags(new HashSet<>(Arrays.asList("Java", "OOP")));
        sampleQuiz.setDifficulty("JUNIOR");
        sampleQuiz.setTimeLimit(30);
    }

    @Test
    void generateQuiz_shouldCreateQuizWithAIGeneratedQuestions() {
        // Given
        when(aiService.generateQuizQuestions(anyString(), anyString(), anyInt()))
                .thenReturn(sampleQuestions);
        when(questionRepository.saveAll(anyList())).thenReturn(sampleQuestions);
        when(quizRepository.save(any(Quiz.class))).thenReturn(sampleQuiz);

        // When
        Quiz result = quizService.generateQuiz(quizRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).contains("Java");
        assertThat(result.getQuestions()).hasSize(3);
        assertThat(result.getTags()).contains("Java", "OOP");

        verify(aiService).generateQuizQuestions("Java, OOP", "JUNIOR", 3);
        verify(questionRepository).saveAll(sampleQuestions);
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void generateQuiz_whenAIServiceFails_shouldUseFallbackQuestions() {
        // Given
        when(aiService.generateQuizQuestions(anyString(), anyString(), anyInt()))
                .thenReturn(new ArrayList<>());
        when(questionRepository.saveAll(anyList())).thenReturn(sampleQuestions);
        when(quizRepository.save(any(Quiz.class))).thenReturn(sampleQuiz);

        // When
        Quiz result = quizService.generateQuiz(quizRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuestions()).isNotNull();

        verify(aiService).generateQuizQuestions(anyString(), anyString(), anyInt());
        verify(questionRepository, atLeastOnce()).saveAll(anyList());
    }

    @Test
    void generateQuiz_withEmptyTags_shouldThrowException() {
        // Given
        quizRequest.setTags(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> quizService.generateQuiz(quizRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tags list cannot be empty");
    }

    @Test
    void generateQuiz_withNullTags_shouldThrowException() {
        // Given
        quizRequest.setTags(null);

        // When & Then
        assertThatThrownBy(() -> quizService.generateQuiz(quizRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tags list cannot be empty");
    }

    @Test
    void evaluateAIQuiz_shouldCalculateCorrectScore() {
        // Given
        Map<String, Object> quizData = new HashMap<>();
        List<Map<String, Object>> questionsData = new ArrayList<>();

        // Question 1 - correct
        Map<String, Object> question1 = new HashMap<>();
        question1.put("text", "What is Java?");
        question1.put("correctAnswer", "A");
        question1.put("explanation", "Java is a programming language");
        question1.put("options", Arrays.asList("Language", "Coffee", "Island", "Car"));
        questionsData.add(question1);

        // Question 2 - incorrect
        Map<String, Object> question2 = new HashMap<>();
        question2.put("text", "What is OOP?");
        question2.put("correctAnswer", "B");
        question2.put("explanation", "OOP is programming paradigm");
        question2.put("options", Arrays.asList("Object", "Paradigm", "Language", "Tool"));
        questionsData.add(question2);

        quizData.put("questions", questionsData);

        Map<String, String> userAnswers = new HashMap<>();
        userAnswers.put("1", "A"); // correct
        userAnswers.put("2", "C"); // incorrect

        // When
        QuizResult result = quizService.evaluateAIQuiz(quizData, userAnswers);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getScore()).isEqualTo(1);
        assertThat(result.getTotalQuestions()).isEqualTo(2);
        assertThat(result.getUserAnswers()).hasSize(2);
    }

    @Test
    void evaluateAIQuiz_withAllCorrectAnswers_shouldReturnPerfectScore() {
        // Given
        Map<String, Object> quizData = new HashMap<>();
        List<Map<String, Object>> questionsData = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Map<String, Object> question = new HashMap<>();
            question.put("text", "Question " + i);
            question.put("correctAnswer", "A");
            question.put("explanation", "Explanation " + i);
            question.put("options", Arrays.asList("Correct", "Wrong1", "Wrong2", "Wrong3"));
            questionsData.add(question);
        }

        quizData.put("questions", questionsData);

        Map<String, String> userAnswers = new HashMap<>();
        userAnswers.put("1", "A");
        userAnswers.put("2", "A");
        userAnswers.put("3", "A");

        // When
        QuizResult result = quizService.evaluateAIQuiz(quizData, userAnswers);

        // Then
        assertThat(result.getScore()).isEqualTo(3);
        assertThat(result.getTotalQuestions()).isEqualTo(3);
    }

    @Test
    void evaluateAIQuiz_withNoAnswers_shouldReturnZeroScore() {
        // Given
        Map<String, Object> quizData = new HashMap<>();
        List<Map<String, Object>> questionsData = new ArrayList<>();

        Map<String, Object> question = new HashMap<>();
        question.put("text", "Question 1");
        question.put("correctAnswer", "A");
        questionsData.add(question);

        quizData.put("questions", questionsData);

        Map<String, String> userAnswers = new HashMap<>(); // empty answers

        // When
        QuizResult result = quizService.evaluateAIQuiz(quizData, userAnswers);

        // Then
        assertThat(result.getScore()).isZero();
        assertThat(result.getTotalQuestions()).isEqualTo(1);
    }

    @Test
    void getQuizById_shouldReturnQuizResponse() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(sampleQuiz));

        // When
        QuizResponse result = quizService.getQuizById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Тест: Java, OOP");
        assertThat(result.getQuestions()).hasSize(3);
    }

    @Test
    void getQuizById_whenQuizNotFound_shouldThrowException() {
        // Given
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> quizService.getQuizById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quiz not found");
    }

    @Test
    void deleteQuizById_shouldDeleteExistingQuiz() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(sampleQuiz));
        doNothing().when(quizRepository).deleteById(1L);

        // When
        String result = quizService.deleteQuizById(1L);

        // Then
        assertThat(result).isEqualTo("Quiz deleted");
        verify(quizRepository).deleteById(1L);
    }

    @Test
    void deleteQuizById_whenQuizNotFound_shouldThrowException() {
        // Given
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> quizService.deleteQuizById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quiz not found");
    }

    @Test
    void convertToDTO_shouldConvertQuizToResponse() {
        // When
        QuizResponse result = quizService.convertToDTO(sampleQuiz);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Тест: Java, OOP");
        assertThat(result.getDescription()).isEqualTo("Автоматически сгенерированный тест");
        assertThat(result.getTags()).contains("Java", "OOP");
        assertThat(result.getDifficulty()).isEqualTo("JUNIOR");
        assertThat(result.getTimeLimit()).isEqualTo(30);
        assertThat(result.getQuestions()).hasSize(3);

        // Verify first question conversion
        QuestionDTO firstQuestion = result.getQuestions().get(0);
        assertThat(firstQuestion.getText()).isEqualTo("Question 1");
        assertThat(firstQuestion.getCorrectAnswer()).isEqualTo("A");
    }

    @Test
    void getQuizzes_shouldReturnAllQuizsAsResponses() {
        // Given
        List<Quiz> quizzes = Arrays.asList(sampleQuiz);
        when(quizRepository.findAll()).thenReturn(quizzes);

        // When
        List<QuizResponse> result = quizService.getQuizzes();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }
}