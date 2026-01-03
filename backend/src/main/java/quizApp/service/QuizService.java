package quizApp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizApp.model.Question;
import quizApp.model.QuestionType;
import quizApp.model.Quiz;
import quizApp.model.QuizResult;
import quizApp.model.dto.QuestionDTO;
import quizApp.model.dto.QuizRequest;
import quizApp.model.dto.QuizResponse;
import quizApp.repository.QuestionRepository;
import quizApp.repository.QuizRepository;
import quizApp.repository.QuizResultRepository;

import java.util.*;
@Slf4j
@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private AIService aiService;


    @Transactional
    public String deleteQuizById(Long id) {

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found " + id));
        quizRepository.deleteById(id);
        return "Quiz deleted";
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzes() {
        List<Quiz> quizzes = quizRepository.findAll();


        for (Quiz quiz : quizzes) {
            // Инициализируем вопросы
            if (quiz.getQuestions() != null) {
                quiz.getQuestions().size(); // Это загрузит коллекцию
            }
            // Инициализируем теги
            if (quiz.getTags() != null) {
                quiz.getTags().size(); // Это загрузит коллекцию
            }
        }

        log.info("quiz service findAll: {}", quizzes);
        List<QuizResponse> result = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            result.add(convertToDTO(quiz));
        }
        log.info("quiz service: ", result);
        return result;
    }

    @Transactional
    public Quiz generateQuiz(QuizRequest request) {
        log.info("Received generate request: {}", request);
        log.info("Tags: {}", request.getTags());
        log.info("Difficulty: {}", request.getDifficulty());
        log.info("QuestionCount: {}", request.getQuestionCount());

        try {
            // Проверка на null и пустой список
            if (request.getTags() == null || request.getTags().isEmpty()) {
                log.warn("Empty tags list provided in request");
                throw new IllegalArgumentException("Tags list cannot be empty");
            }
            String tagsString = String.join(", ", request.getTags());

            String prompt = request.getTags() + " - создай " + request.getQuestionCount() + " вопросов";
            log.debug("Generating quiz with prompt: {}", prompt);

            List<Question> questions = aiService.generateQuizQuestions(tagsString, request.getDifficulty(), request.getQuestionCount());
            questions = questionRepository.saveAll(questions);

            if (questions.isEmpty()) {
                log.warn("AI service returned empty questions list, creating sample questions");
                // Если вопросов нет, создаем заглушки и СОХРАНЯЕМ их
                questions = createSampleQuestions(request.getQuestionCount());
                questions = questionRepository.saveAll(questions); // Сохраняем вопросы перед созданием теста
            } else {
                log.info("Successfully generated {} questions", questions.size());
            }

            Quiz quiz = new Quiz();
            quiz.setTitle("Тест: " + String.join(", ", request.getTags()));
            quiz.setDescription("Автоматически сгенерированный тест");
            quiz.setQuestions(questions);
            quiz.setTags(new HashSet<>(request.getTags()));
            quiz.setDifficulty(request.getDifficulty());
            quiz.setTimeLimit(30);

            Quiz savedQuiz = quizRepository.save(quiz);
            log.info("Successfully created quiz with ID: {}", savedQuiz.getId());

            return savedQuiz;

        } catch (Exception e) {
            log.error("Error generating quiz for request: {}", request, e);
            throw e;
        }
    }


    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        return convertToDTO(quiz);
    }

    // Новый метод для оценки AI тестов
    public QuizResult evaluateAIQuiz(Map<String, Object> quizData, Map<String, String> userAnswers) {
        int score = 0;
        int totalQuestions = 0;
        Map<Question, String> answersMap = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questionsData = (List<Map<String, Object>>) quizData.get("questions");

            if (questionsData != null) {
                totalQuestions = questionsData.size();

                for (int i = 0; i < questionsData.size(); i++) {
                    Map<String, Object> questionData = questionsData.get(i);

                    // Используем ID или индекс как ключ
                    String questionId = questionData.get("id") != null ?
                            questionData.get("id").toString() : String.valueOf(i + 1);

                    String questionKey = String.valueOf(i + 1);
                    String userAnswer = userAnswers.get(questionKey);
                    String correctAnswer = (String) questionData.get("correctAnswer");

                    // Создаем объект Question для результата
                    Question question = new Question();
                    question.setText((String) questionData.get("text"));
                    question.setCorrectAnswer(correctAnswer);
                    question.setExplanation((String) questionData.get("explanation"));

                    @SuppressWarnings("unchecked")
                    List<String> options = (List<String>) questionData.get("options");
                    if (options != null) {
                        question.setOptions(options);
                    }

                    answersMap.put(question, userAnswer);

                    // Проверяем правильность ответа
                    if (correctAnswer != null && correctAnswer.equals(userAnswer)) {
                        score++;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error processing AI quiz: {} ", e.getMessage());
            e.printStackTrace();
        }

        QuizResult result = new QuizResult();
        result.setScore(score);
        result.setTotalQuestions(totalQuestions);
        result.setUserAnswers(answersMap);
        result.setAttemptNumber(1);

        return result;
    }

    @Transactional
    private List<Question> createSampleQuestions(int count) {
        List<Question> questions = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Question question = new Question();
            question.setText("Тестовый вопрос из quizService " + i + " о Java программировании");
            question.setType(QuestionType.THEORY);
            question.setOptions(Arrays.asList("Вариант A", "Вариант B", "Вариант C", "Вариант D"));
            question.setCorrectAnswer("A");
            question.setExplanation("Это объяснение для тестового вопроса " + i);
            question.setTags(Set.of("Java", "Тест"));
            question.setDifficulty("JUNIOR");

            questions.add(question);
        }
        return questions;
    }

    @Transactional(readOnly = true)
    public QuizResponse convertToDTO(Quiz quiz) {
        QuizResponse response = new QuizResponse();
        log.info("convert to dto Quiz Response response: {} ", response);

        response.setId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setDescription(quiz.getDescription());
        response.setTags(quiz.getTags());
        response.setDifficulty(quiz.getDifficulty());
        response.setTimeLimit(quiz.getTimeLimit());


        // Вам нужно будет создать метод для конвертации Question в QuestionDTO
        response.setQuestions(convertQuestionsToDTO(quiz.getQuestions()));
        log.info("convert to dto: {} ", response);
        return response;
    }

    private List<QuestionDTO> convertQuestionsToDTO(List<Question> questions) {
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (Question q : questions) {
            QuestionDTO questionDTO = new QuestionDTO();
            questionDTO.setId(q.getId());
            questionDTO.setTags(q.getTags());
            questionDTO.setDifficulty(q.getDifficulty());
            questionDTO.setCorrectAnswer(q.getCorrectAnswer());
            questionDTO.setOptions(q.getOptions());
            questionDTO.setExplanation(q.getExplanation());
            questionDTO.setText(q.getText());

            questionDTOList.add(questionDTO);
        }
        return questionDTOList;
    }


}