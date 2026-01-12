package quizApp.model.dto;

import java.util.List;

public class QuizRequest {
    private List<String> tags;
    private String difficulty;
    private Integer questionCount;

    public QuizRequest() {
    }

    public QuizRequest(List<String> tags, String difficulty, Integer questionCount) {
        this.tags = tags;
        this.difficulty = difficulty;
        this.questionCount = questionCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
}