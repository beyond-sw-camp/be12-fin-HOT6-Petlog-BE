package com.hot6.backend.mongo.migration;

import com.hot6.backend.chat.model.ChatRoom;
import com.hot6.backend.chat.model.ChatRoomHashtag;
import com.hot6.backend.chat.repository.ChatRoomRepository;
import com.hot6.backend.mongo.model.Counter;
import com.hot6.backend.mongo.room.MongoChatRoomDocument;
import com.hot6.backend.mongo.room.MongoChatRoomRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatRoomMigration {

    private final ChatRoomRepository chatRoomRepository;
    private final MongoChatRoomRepository mongoChatRoomRepository;
    private final MongoTemplate mongoTemplate;

    @Transactional(readOnly = true)
    @PostConstruct
    public void migrate() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllWithParticipantsAndHashtags();

        List<MongoChatRoomDocument> docs = chatRooms.stream().map(room -> {
            List<MongoChatRoomDocument.MongoParticipant> participants = room.getParticipants().stream()
                    .filter(p -> !p.isDeleted())
                    .map(p -> MongoChatRoomDocument.MongoParticipant.builder()
                            .userId(p.getUser().getIdx())
                            .metaData(p.getMetaData())
                            .nickname(p.getUser().getNickname())
                            .profileImage(p.getUser().getUserProfileImage())
                            .isAdmin(p.getIsAdmin())
                            .build())
                    .collect(Collectors.toList());

            List<String> hashtags = room.getHashtags().stream()
                    .map(ChatRoomHashtag::getCTag)
                    .collect(Collectors.toList());

            return MongoChatRoomDocument.builder()
                    .idx(room.getIdx())
                    .title(room.getCTitle())
                    .maxParticipants(room.getMaxParticipants())
                    .participants(participants)
                    .hashtags(hashtags)
                    .build();
        }).toList();

        List<MongoChatRoomDocument> documents = mongoChatRoomRepository.saveAll(docs);
        System.out.println("✅ 총 " + documents.size() + "개의 채팅방을 MongoDB로 마이그레이션 완료.");

        // ✅ 마지막 idx 기준으로 counter 초기화
        long maxIdx = documents.stream()
                .mapToLong(MongoChatRoomDocument::getIdx)
                .max()
                .orElseThrow(() -> new IllegalStateException("마이그레이션된 채팅방이 없습니다."));

        Query query = new Query(Criteria.where("_id").is("chat_room"));
        Update update = new Update().set("seq", maxIdx);
        mongoTemplate.upsert(query, update, Counter.class);

        System.out.println("📌 counter(chat_room) 초기화 완료 → seq = " + maxIdx);
    }
}
