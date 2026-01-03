package quizApp.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "quiz_results")
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private Integer score;
    private Integer totalQuestions;
    private LocalDateTime completedAt;
    private Integer attemptNumber;

    @ElementCollection
    @CollectionTable(name = "user_answers", joinColumns = @JoinColumn(name = "result_id"))
    @MapKeyJoinColumn(name = "question_id")
    @Column(name = "user_answer")
    private Map<Question, String> userAnswers;

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public QuizResult() {
    }

    public QuizResult(Quiz quiz, Integer score, Integer totalQuestions,
                      Integer attemptNumber, Map<Question, String> userAnswers, User user) {
        this.quiz = quiz;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.attemptNumber = attemptNumber;
        this.userAnswers = userAnswers;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public Map<Question, String> getUserAnswers() {
        return userAnswers;
    }

    public void setUserAnswers(Map<Question, String> userAnswers) {
        this.userAnswers = userAnswers;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}