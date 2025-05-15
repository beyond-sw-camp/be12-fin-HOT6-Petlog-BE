package com.hot6.backend.board.answer;

import com.hot6.backend.board.answer.model.Answer;
import com.hot6.backend.board.question.model.Question;
import com.hot6.backend.user.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestion_IdxOrderByCreatedAtDesc(Long questionIdx);

    List<Answer> findByUser_IdxOrderByCreatedAtDesc(Long userId);

    int countByQuestion(Question question);

    int countByQuestion_IdxAndUser_UserTypeNot(Long questionIdx, UserType userType);

    @Query("""
    SELECT DISTINCT a FROM Answer a
    LEFT JOIN FETCH a.user
    LEFT JOIN FETCH a.question
    LEFT JOIN FETCH a.answerImageList
    WHERE a.question.idx = :questionIdx
    ORDER BY a.createdAt DESC
""")
    List<Answer> findByQuestionIdxWithAssociations(@Param("questionIdx") Long questionIdx);

}
