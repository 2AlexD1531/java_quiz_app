package quizApp.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quizApp.model.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q JOIN q.tags t WHERE t = :tag AND q.difficulty = :difficulty")
    List<Question> findByTagsInAndDifficulty(
            @Param("tag") String tag,
            @Param("difficulty") String difficulty,
            Pageable pageable
    );

    List<Question> findByTagsContaining(String tag);

    @Query("SELECT DISTINCT q.tags FROM Question q")
    List<String> findAllDistinctTags();
}