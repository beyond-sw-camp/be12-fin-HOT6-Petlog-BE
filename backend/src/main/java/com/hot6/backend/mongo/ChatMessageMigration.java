package com.hot6.backend.mongo;

import com.hot6.backend.chat.model.Chat;
import com.hot6.backend.chat.model.ChatRoom;
import com.hot6.backend.chat.model.ChatRoomParticipant;
import com.hot6.backend.chat.repository.ChatRepository;
import com.hot6.backend.user.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
@RequiredArgsConstructor
public class ChatMessageMigration {

    private final ChatRepository chatRepository;
    private final MongoChatMessageRepository mongoChatMessageRepository;

//    @PostConstruct
    public void migrateChatMessages() {
        List<Chat> chats = chatRepository.findAllWithJoinFetch(); // 💡 필요시 배치 처리 권장

        List<MongoChatMessageDocument> documents = chats.stream().map(chat -> {
            ChatRoomParticipant participant = chat.getChatRoomParticipant();
            User sender = participant.getUser();
            ChatRoom room = participant.getChatRoom();

            return MongoChatMessageDocument.builder()
                    .idx(chat.getIdx())
                    .roomId(room.getIdx())
                    .senderId(sender.getIdx())
                    .senderNickname(sender.getNickname())
                    .message(chat.getMessage())
                    .isRead(Boolean.TRUE.equals(chat.getCIsRead()))
                    .type(chat.getType())
                    .createdAt(chat.getCreatedAt())
                    .build();
        }).toList();

        mongoChatMessageRepository.saveAll(documents);
        System.out.println("✅ 총 " + documents.size() + "개의 채팅 메시지를 MongoDB로 마이그레이션 완료.");
    }
}