package quizApp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizApp.model.Question;
import quizApp.repository.QuestionRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }

    public List<Question> saveAllQuestions(List<Question> questions) {
        return questionRepository.saveAll(questions);
    }

    public List<String> getAllTags() {
        return questionRepository.findAllDistinctTags();
    }

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    public Question updateQuestion(Long id, Question questionDetails) {
        return questionRepository.findById(id)
                .map(question -> {
                    question.setText(questionDetails.getText());
                    question.setType(questionDetails.getType());
                    question.setOptions(questionDetails.getOptions());
                    question.setCorrectAnswer(questionDetails.getCorrectAnswer());
                    question.setExplanation(questionDetails.getExplanation());
                    question.setTags(questionDetails.getTags());
                    question.setDifficulty(questionDetails.getDifficulty());
                    return questionRepository.save(question);
                })
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));
    }

    public List<Question> getQuestionsByTag(String tag) {
        return questionRepository.findByTagsContaining(tag);
    }

    public List<Question> getQuestionsByDifficulty(String difficulty) {
        return questionRepository.findAll().stream()
                .filter(q -> q.getDifficulty().equalsIgnoreCase(difficulty))
                .toList();
    }

    public long getTotalQuestions() {
        return questionRepository.count();
    }

    public long getQuestionsCountByTag(String tag) {
        return questionRepository.findByTagsContaining(tag).size();
    }

    public long getQuestionsCountByDifficulty(String difficulty) {
        return questionRepository.findAll().stream()
                .filter(q -> q.getDifficulty().equalsIgnoreCase(difficulty))
                .count();
    }
}