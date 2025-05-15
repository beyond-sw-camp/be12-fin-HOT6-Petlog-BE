package com.hot6.backend.board.comment;

import com.hot6.backend.board.answer.model.Answer;
import com.hot6.backend.board.comment.model.Comment;
import com.hot6.backend.board.post.model.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    @Modifying
    @Transactional
    void deleteByPostIdx(Long questionIdx);

    List<Comment> findByUser_IdxOrderByCreatedAtDesc(Long userIdx);

    @Query("""
    SELECT DISTINCT c FROM Comment c
    LEFT JOIN FETCH c.user
    LEFT JOIN FETCH c.post p
    LEFT JOIN FETCH p.boardType
    WHERE c.post = :post
    ORDER BY c.createdAt DESC
""")
    List<Comment> findByPostWithAssociations(@Param("post") Post post);

}
