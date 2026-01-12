package quizApp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import quizApp.model.Question;
import quizApp.model.QuestionType;
import quizApp.repository.QuestionRepository;

import java.util.*;

@Service
@Slf4j
public class AIService {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.model}")
    private String model;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private QuestionRepository questionRepository;


    public List<Question> generateQuizQuestions(String topics, String difficulty, int questionCount) {
        log.info("Generating questions with prompt: {} ", topics);
        String fullPrompt = createPrompt(topics, difficulty, questionCount);

        try {
            String response = callAIAPI(fullPrompt);
            if (response == null) {
                throw new Exception("OpenRouter API returned null response");
            }
            log.info("OpenRouter response received");

            return parseAIResponse(response);
        } catch (Exception e) {
            log.warn("AI Service error: {}", e.getMessage());
            e.printStackTrace();
            return createFallbackQuestions();
        }
    }

    private String createPrompt(String topics, String difficulty, int questionCount) {
        return """
                Ты - эксперт по Java. Составь тест из %d вопросов по Java (для тестирования).
                
                ТЕМЫ: %s
                УРОВЕНЬ: %s
                ГОД: 2025 (актуальные версии Java)
                
                ТРЕБОВАНИЯ К ФОРМАТУ:
                - Каждый вопрос должен иметь 4 варианта ответа (A, B, C, D)
                - Правильный ответ указывай буквой (A, B, C или D)
                - Объяснение должно быть кратким и понятным
                - Используй теги для категоризации вопросов
                - Типы вопросов: THEORY, CODE, OUTPUT, TASK
                
                ВЕРНИ ТОЛЬКО JSON в таком формате:
                {
                  "questions": [
                    {
                      "text": "текст вопроса",
                      "type": "THEORY",
                      "options": ["Вариант A", "Вариант B", "Вариант C", "Вариант D"],
                      "correctAnswer": "A",
                      "explanation": "Объяснение почему этот ответ правильный",
                      "tags": ["String"],
                      "difficulty": "JUNIOR"
                    }
                  ]
                }
                """.formatted(questionCount, difficulty, topics);
    }

    private String callAIAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", "http://localhost:8080");
            headers.set("X-Title", "Java Quiz App");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            requestBody.put("max_tokens", 4000);
            requestBody.put("temperature", 0.7);
            requestBody.put("top_p", 0.9);

            log.info("Sending request to OpenRouter with model: {}", model);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class
            );

            log.info("OpenRouter response status: {} ", response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.warn("OpenRouter API call failed: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private List<Question> parseAIResponse(String response) throws Exception {
        log.info("Parsing AI response: {} ", response.substring(0, Math.min(200, response.length())) + "...");

        JsonNode root = objectMapper.readTree(response);

        if (root.has("error")) {
            String error = root.path("error").path("message").asText();
            throw new Exception("OpenRouter error: " + error);
        }

        JsonNode choices = root.path("choices");
        if (choices.isEmpty()) {
            throw new Exception("No choices in response");
        }

        String content = choices.get(0).path("message").path("content").asText();
        log.info("AI response content: {} ", content.substring(0, Math.min(200, content.length())) + "...");

        String jsonContent = extractJsonFromResponse(content);

        JsonNode contentNode = objectMapper.readTree(jsonContent);
        JsonNode questionsNode = contentNode.path("questions");

        List<Question> questions = new ArrayList<>();

        for (JsonNode questionNode : questionsNode) {
            try {
                Question question = new Question();
                question.setText(questionNode.path("text").asText());

                String typeStr = questionNode.path("type").asText().toUpperCase();
                try {
                    question.setType(QuestionType.valueOf(typeStr));
                } catch (IllegalArgumentException e) {
                    question.setType(QuestionType.THEORY); // значение по умолчанию
                }

                List<String> options = new ArrayList<>();
                questionNode.path("options").forEach(option -> options.add(option.asText()));
                question.setOptions(options);

                question.setCorrectAnswer(questionNode.path("correctAnswer").asText());
                question.setExplanation(questionNode.path("explanation").asText());

                Set<String> tags = new HashSet<>();
                questionNode.path("tags").forEach(tag -> tags.add(tag.asText()));
                question.setTags(tags);

                question.setDifficulty(questionNode.path("difficulty").asText("JUNIOR"));

                questions.add(question);
                log.info("Parsed question: {}", question.getText());

            } catch (Exception e) {
                log.warn("Error parsing question: {} ", e.getMessage());
            }
        }
        log.info("Successfully parsed {} questions", questions.size());
        return questions;
    }

    private String extractJsonFromResponse(String content) {
        int jsonStart = content.indexOf('{');
        int jsonEnd = content.lastIndexOf('}');

        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd + 1);
        }
        return content;
    }

    private List<Question> createFallbackQuestions() {
        log.info("Using fallback questions");
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setText("Что такое String в Java?");
        q1.setType(QuestionType.THEORY);
        q1.setOptions(Arrays.asList(
                "Примитивный тип данных",
                "Класс для работы со строками",
                "Интерфейс",
                "Аннотация"
        ));
        q1.setCorrectAnswer("B");
        q1.setExplanation("String - это final класс в Java, а не примитивный тип");
        q1.setTags(Set.of("String", "Basics"));
        q1.setDifficulty("JUNIOR");
        questions.add(q1);

        Question q2 = new Question();
        q2.setText("Как создать массив из 5 целых чисел?");
        q2.setType(QuestionType.CODE);
        q2.setOptions(Arrays.asList(
                "int[] arr = new int[5];",
                "int arr = new int[5];",
                "array arr = new array(5);",
                "int arr[5];"
        ));
        q2.setCorrectAnswer("A");
        q2.setExplanation("Правильный синтаксис: тип[] имя = new тип[размер]");
        q2.setTags(Set.of("Arrays", "Syntax"));
        q2.setDifficulty("JUNIOR");
        questions.add(q2);

        Question q3 = new Question();
        q3.setText("Что выведет код: System.out.println(10 + 20 + \"30\");");
        q3.setType(QuestionType.OUTPUT);
        q3.setOptions(Arrays.asList(
                "102030",
                "3030",
                "60",
                "Ошибка компиляции"
        ));
        q3.setCorrectAnswer("B");
        q3.setExplanation("Сначала выполняется сложение 10+20=30, затем конкатенация \"30\" + \"30\" = \"3030\"");
        q3.setTags(Set.of("Output", "Operations"));
        q3.setDifficulty("JUNIOR");
        questions.add(q3);

        return questions;
    }

    public void quizConnection() {

        try {
            log.info("Quizing OpenRouter connection...");
            log.info("API Key: {}", apiKey != null ? "SET" : "MISSING");
            log.info("API URL: {}", apiUrl);
            log.info("Model: {}", model);

            String quizPrompt = "Ответь просто 'OK'";
            String response = callAIAPI(quizPrompt);

            if (response != null) {
                log.info("OpenRouter connection quiz: SUCCESS");
                log.info("Response: {}", response.substring(0, Math.min(100, response.length())));
            } else {
                log.warn("OpenRouter connection quiz: FAILED - null response");
            }
        } catch (Exception e) {
            log.warn("Connection quiz failed: {}", e.getMessage(), e);
        }
    }
}