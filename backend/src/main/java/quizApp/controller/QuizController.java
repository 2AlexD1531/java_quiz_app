package quizApp.controller;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizApp.model.Quiz;
import quizApp.model.QuizResult;
import quizApp.model.dto.QuizRequest;
import quizApp.model.dto.QuizResponse;
import quizApp.service.AIService;
import quizApp.service.QuizService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    @GetMapping("/quiz-ai")
    public ResponseEntity<String> quizAI() {
        aiService.quizConnection();
        return ResponseEntity.ok("AI connection quiz completed");
    }

    @Autowired
    private QuizService quizService;

    @Autowired
    private AIService aiService;

    @PostMapping("/generate")
    @Transactional
    public ResponseEntity<QuizResponse> generateQuiz(@RequestBody QuizRequest request) {
        log.info("Received generate request: {}", request);

        Quiz quiz = quizService.generateQuiz(request);
        QuizResponse response = quizService.convertToDTO(quiz);
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuiz(@PathVariable Long id) {
        QuizResponse quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }

    @DeleteMapping("/delete-quiz/{id}")
    public ResponseEntity<?> delQuiz(@PathVariable Long id) {

        String result = quizService.deleteQuizById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-quizzes")
    public ResponseEntity<List<QuizResponse>> getAllQuizzes() {

            List<QuizResponse> quizzesList = quizService.getQuizzes();
            log.info("Retrieved {} quizzes", quizzesList.size());
            return ResponseEntity.ok(quizzesList);
    }

    @PostMapping("/ai-submit")
    public ResponseEntity<QuizResult> submitAIQuiz(@RequestBody Map<String, Object> submission) {

        log.info("Received AI submission with keys: {}", submission.keySet());

        @SuppressWarnings("unchecked")
        Map<String, String> userAnswers = (Map<String, String>) submission.get("answers");
        log.debug("User answers: {}", userAnswers);

        @SuppressWarnings("unchecked")
        Map<String, Object> quizData = (Map<String, Object>) submission.get("quizData");
        log.debug("Quiz data keys: {}", quizData.keySet());

        QuizResult result = quizService.evaluateAIQuiz(quizData, userAnswers);
        log.info("AI quiz evaluation completed successfully");
        return ResponseEntity.ok(result);
    }
}