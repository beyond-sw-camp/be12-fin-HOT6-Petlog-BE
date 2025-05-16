package com.hot6.backend.chat.repository;

import com.hot6.backend.chat.model.Chat;
import com.hot6.backend.mongo.migration.ChatMessageProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {


    @Query("""
    SELECT new com.hot6.backend.mongo.migration.ChatMessageProjectionImpl(
        c.idx, c.message, c.cIsRead, c.type, c.createdAt,
        u.idx, u.nickname, r.idx
    )
    FROM Chat c
    JOIN c.chatRoomParticipant p
    JOIN p.user u
    LEFT JOIN u.emailVerify
    JOIN p.chatRoom r
    """)
    Page<ChatMessageProjection> findAllProjected(Pageable pageable);
}
