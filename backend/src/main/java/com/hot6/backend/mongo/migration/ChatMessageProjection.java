package com.hot6.backend.mongo.migration;

import com.hot6.backend.chat.model.ChatMessageType;

import java.time.LocalDateTime;

public interface ChatMessageProjection {
    Long getIdx();
    String getMessage();
    Boolean getCIsRead();
    ChatMessageType getType();
    LocalDateTime getCreatedAt();

    Long getSenderId();
    String getSenderNickname();
    Long getRoomId();
}
