package quizApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quizApp.model.QuizResult;
import quizApp.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findByUser(User user);

    Optional<QuizResult> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT tr.user.username, COUNT(tr) FROM QuizResult tr GROUP BY tr.user.username ORDER BY COUNT(tr) DESC")
    List<Object[]> findMostActiveUsers();

    @Query("SELECT AVG(tr.score) FROM QuizResult tr")
    Double findAverageScore();

    @Query("SELECT COUNT(tr) FROM QuizResult tr WHERE tr.score >= :minScore")
    Long countByMinScore(@Param("minScore") Integer minScore);

    List<QuizResult> findByOrderByCompletedAtDesc();
}