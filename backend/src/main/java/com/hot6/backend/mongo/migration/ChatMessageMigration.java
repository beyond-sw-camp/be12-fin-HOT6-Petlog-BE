package com.hot6.backend.mongo.migration;

import com.hot6.backend.chat.model.Chat;
import com.hot6.backend.chat.model.ChatMessageType;
import com.hot6.backend.chat.model.ChatRoom;
import com.hot6.backend.chat.model.ChatRoomParticipant;
import com.hot6.backend.chat.repository.ChatRepository;
import com.hot6.backend.mongo.message.model.MongoChatMessageDocument;
import com.hot6.backend.mongo.message.repository.MongoChatMessageRepository;
import com.hot6.backend.mongo.model.Counter;
import com.hot6.backend.user.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMessageMigration {
    private final MongoTemplate mongoTemplate;
    private final ChatRepository chatRepository;
    private final MongoChatMessageRepository mongoChatMessageRepository;

    @PostConstruct
    public void migrateChatMessages() {
        int page = 0;
        int size = 1000;

        Page<ChatMessageProjection> chatPage;

        do {
            Pageable pageable = PageRequest.of(page, size);
            chatPage = chatRepository.findAllProjected(pageable);

            List<MongoChatMessageDocument> docs = chatPage.getContent().stream()
                    .map(chat -> {
                        return MongoChatMessageDocument.builder()
                                .idx(chat.getIdx())
                                .roomId(chat.getRoomId())
                                .senderId(chat.getSenderId())
                                .senderNickname(chat.getSenderNickname())
                                .message(chat.getMessage())
                                .isRead(Boolean.TRUE.equals(chat.getCIsRead()))
                                .type(chat.getType())
                                .createdAt(chat.getCreatedAt())
                                .build();
                    }).toList();

            mongoChatMessageRepository.saveAll(docs);
            System.out.println("✅ page " + page + " migrated: " + docs.size());

            page++;
        } while (!chatPage.isLast());

        // counter(chat_message_idx) 초기화
        long maxIdx = mongoChatMessageRepository.findTopByOrderByIdxDesc()
                .map(MongoChatMessageDocument::getIdx)
                .orElseThrow(() -> new IllegalStateException("마이그레이션된 메시지가 없습니다."));

        Query query = new Query(Criteria.where("_id").is("chat_message_idx"));
        Update update = new Update().set("seq", maxIdx);
        mongoTemplate.upsert(query, update, Counter.class);

        System.out.println("📌 counter(chat_message_idx) 초기화 완료 → seq = " + maxIdx);
    }
}