package com.hot6.backend.chat.repository;

import com.hot6.backend.chat.model.Chat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("""
    SELECT c FROM Chat c
    JOIN FETCH c.chatRoomParticipant p
    JOIN FETCH p.user u
    JOIN FETCH p.chatRoom r
    """)
    List<Chat> findAllWithJoinFetch();
}
