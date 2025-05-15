package com.hot6.backend.chat.repository;

import com.hot6.backend.chat.model.Chat;
import com.hot6.backend.chat.model.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<Chat,Long> {
    @Query("SELECT c FROM Chat c " +
            "WHERE c.chatRoomParticipant.chatRoom.idx = :roomId " +
            "AND c.idx >= :startMessageId " +
            "ORDER BY c.createdAt ASC")
    List<Chat> findByChatRoomIdAndIdxGreaterThanEqualOrderByTimestampAsc(Long roomId, Long startMessageId);

    @Query("SELECT c FROM Chat c " +
            "WHERE c.chatRoomParticipant.chatRoom.idx = :roomId " +
            "AND c.idx >= :startMessageId " +
            "AND (:lastMessageId IS NULL OR c.idx < :lastMessageId) " +
            "ORDER BY c.idx DESC")
    Slice<Chat> findSliceForScroll(
            Long roomId,
            Long startMessageId,
            Long lastMessageId,
            Pageable pageable
    );


    @Query(value = """
    SELECT c.* 
    FROM chat c
    JOIN chat_room_participant crp ON c.chat_room_parti_idx = crp.idx
    WHERE crp.chat_room_idx = :chatRoomIdx
    ORDER BY c.idx DESC
    LIMIT 1
    """, nativeQuery = true)
    Optional<Chat> findTopByChatRoomParticipant_ChatRoomOrderByIdxDesc(Long chatRoomIdx);
}
