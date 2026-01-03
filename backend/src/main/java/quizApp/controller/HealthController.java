package quizApp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping({"/api/health", "/health"})
    public String healthCheck() {
        return "Java Quiz App is running!";
    }
}