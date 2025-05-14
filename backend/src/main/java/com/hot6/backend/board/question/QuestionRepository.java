package com.hot6.backend.board.question;

import com.hot6.backend.board.question.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByUser_IdxOrderByCreatedAtDesc(Long userIdx);

    Page<Question> findAll(Pageable pageable);

    @Query("""
              SELECT DISTINCT q FROM Question q
              LEFT JOIN q.hashtagsList h
              WHERE q.qTitle LIKE %:keyword%
                 OR q.content LIKE %:keyword%
                 OR q.user.nickname LIKE %:keyword%
                 OR h.tag LIKE %:keyword%
            """)
    Page<Question> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
