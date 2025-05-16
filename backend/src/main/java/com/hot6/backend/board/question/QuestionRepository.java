package com.hot6.backend.board.question;

import com.hot6.backend.board.question.model.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByUser_IdxOrderByCreatedAtDesc(Long userIdx);

    @Query("""
        SELECT DISTINCT q FROM Question q
        LEFT JOIN FETCH q.user
        LEFT JOIN FETCH q.hashtagsList
        ORDER BY q.createdAt DESC
    """)
    List<Question> findAllWithHashtags(Pageable pageable);

    @Query("SELECT COUNT(q) FROM Question q")
    long countAll();

    @Query("""
        SELECT DISTINCT q FROM Question q
        LEFT JOIN FETCH q.user
        LEFT JOIN FETCH q.hashtagsList
        WHERE q.qTitle LIKE %:keyword%
           OR q.content LIKE %:keyword%
           OR q.user.nickname LIKE %:keyword%
           OR EXISTS (
               SELECT 1 FROM q.hashtagsList h WHERE h.tag LIKE %:keyword%
           )
        ORDER BY q.createdAt DESC
    """)
    List<Question> searchWithHashtags(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT q) FROM Question q
        LEFT JOIN q.hashtagsList h
        WHERE q.qTitle LIKE %:keyword%
           OR q.content LIKE %:keyword%
           OR q.user.nickname LIKE %:keyword%
           OR h.tag LIKE %:keyword%
    """)
    long countByKeyword(@Param("keyword") String keyword);

    @Query("""
        SELECT q FROM Question q
        LEFT JOIN FETCH q.user
        LEFT JOIN FETCH q.questionImageList
        WHERE q.idx = :idx
    """)
    Optional<Question> findWithAssociationsById(@Param("idx") Long idx);
}
