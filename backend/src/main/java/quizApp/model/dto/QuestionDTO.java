package quizApp.model.dto;


import quizApp.model.QuestionType;

import java.util.List;
import java.util.Set;

public class QuestionDTO {
    private Long id;
    private String text;
    private QuestionType type;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private Set<String> tags;
    private String difficulty;

    public QuestionDTO() {
    }

    public QuestionDTO(Long id, String text, QuestionType type, List<String> options,
                       String correctAnswer, String explanation, Set<String> tags,
                       String difficulty) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.tags = tags;
        this.difficulty = difficulty;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
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
}