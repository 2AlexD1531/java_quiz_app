package quizApp.model.dto;

import java.util.List;
import java.util.Set;

public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private List<QuestionDTO> questions;
    private Set<String> tags;
    private String difficulty;
    private Integer timeLimit;

    public QuizResponse() {
    }

    public QuizResponse(Long id, String title, String description, List<QuestionDTO> questions,
                        Set<String> tags, String difficulty, Integer timeLimit) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.questions = questions;
        this.tags = tags;
        this.difficulty = difficulty;
        this.timeLimit = timeLimit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }
}