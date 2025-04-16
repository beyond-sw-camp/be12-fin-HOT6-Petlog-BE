package com.hot6.backend.chat.repository;

import com.hot6.backend.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>,ChatRoomRepositoryCustom  {
    @Query("""
    SELECT DISTINCT cr
    FROM ChatRoom cr
    JOIN FETCH cr.participants p
    JOIN FETCH cr.hashtags h
    WHERE cr IN (
        SELECT cp.chatRoom
        FROM ChatRoomParticipant cp
        WHERE cp.user.idx = :userIdx
    )
""")
    List<ChatRoom> findAllByParticipants(Long userIdx);

    // ChatRoomRepositoryCustom.java
    List<ChatRoom> findChatRoomsWithDetailsByIds(List<Long> roomIds);
}
