package com.hot6.backend.mongo;

import com.hot6.backend.chat.model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoChatRoomRepository extends MongoRepository<MongoChatRoomDocument, Long> {
    // 전체 목록 조회
    List<MongoChatRoomDocument> findAll();

    // 단건 조회
    Optional<MongoChatRoomDocument> findByIdx(Long idx);

    // 사용자 기준으로 내가 속한 방
    List<MongoChatRoomDocument> findByParticipantsUserId(Long userId);

    List<MongoChatRoomDocument> findByTitleContainingAndHashtagsIn(String title, List<String> hashtags);

    // 단일 조건
    List<MongoChatRoomDocument> findByTitleContaining(String keyword);

    List<MongoChatRoomDocument> findByHashtagsIn(List<String> hashtags);
}
