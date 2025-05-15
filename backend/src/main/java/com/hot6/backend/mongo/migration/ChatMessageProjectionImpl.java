
package com.hot6.backend.mongo.migration;

import com.hot6.backend.chat.model.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageProjectionImpl implements ChatMessageProjection {
    private Long idx;
    private String message;
    private Boolean cIsRead;
    private ChatMessageType type;
    private LocalDateTime createdAt;

    private Long senderId;
    private String senderNickname;
    private Long roomId;


}