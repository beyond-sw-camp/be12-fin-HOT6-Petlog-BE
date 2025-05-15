package com.hot6.backend.chat.repository;

import com.hot6.backend.chat.model.ChatRoom;
import com.hot6.backend.chat.model.ChatRoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom  {
    @Query("""
    SELECT cr FROM ChatRoom cr
    LEFT JOIN FETCH cr.participants p
    LEFT JOIN FETCH p.user
    LEFT JOIN FETCH cr.hashtags
""")
    List<ChatRoom> findAllWithParticipantsAndHashtags();

    @Query("SELECT cr FROM ChatRoom cr")
    Slice<ChatRoom> findAllWithSlice(Pageable pageable);

    Optional<ChatRoom> findByIdx(Long idx);

    @Query("SELECT DISTINCT r FROM ChatRoom r " +
            "LEFT JOIN FETCH r.participants p " +
            "LEFT JOIN FETCH r.hashtags h " +
            "WHERE r.idx = :roomId")
    Optional<ChatRoom> findWithParticipantsAndHashtagsById(Long roomId);

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
    Slice<ChatRoom> findChatRoomsWithDetailsByIds(List<Long> roomIds, Pageable pageable);
    List<ChatRoom> findByTagsWithParticipants(List<String> tagList);
    List<ChatRoom> findByTitleWithParticipantsAndTags(String keyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ChatRoom c where c.idx = :idx")
    Optional<ChatRoom> findByIdxForUpdate(Long idx);
    List<ChatRoom> findByType(ChatRoomType type);
}
