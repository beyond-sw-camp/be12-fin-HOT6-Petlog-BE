package com.hot6.backend.mongo.message.model;

import com.hot6.backend.chat.model.ChatMessageType;
import com.hot6.backend.common.BaseEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class MongoChatMessageDocument {

    @Id
    private Long idx; // 기존 Chat ID 그대로

    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String message;
    private boolean isRead;
    private ChatMessageType type;
    private LocalDateTime createdAt;
}
