package com.hot6.backend.board.post;

import com.hot6.backend.board.post.model.BoardType;
import com.hot6.backend.board.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUserIdxAndIsDeletedFalseOrderByCreatedAtDesc(Long userIdx);

    Page<Post> findByBoardType(BoardType boardType, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.boardType = :boardType AND p.category.name = :categoryName
              AND (
                p.title LIKE %:keyword%
                OR p.user.nickname LIKE %:keyword%
              )
            """)
    Page<Post> searchByCategoryAndKeyword(
            @Param("boardType") BoardType boardType,
            @Param("categoryName") String categoryName,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Page<Post> findByBoardTypeAndCategoryName(
            BoardType boardType, String categoryName, Pageable pageable
    );

    @Query("""
            SELECT p FROM Post p
            WHERE p.boardType = :boardType
              AND (
                p.title LIKE %:keyword%
                OR p.user.nickname LIKE %:keyword%
              )
            """)
    Page<Post> searchByKeywordOnly(
            @Param("boardType") BoardType boardType,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}
