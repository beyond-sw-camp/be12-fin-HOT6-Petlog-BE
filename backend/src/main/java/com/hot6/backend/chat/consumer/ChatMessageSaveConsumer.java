package com.hot6.backend.chat.consumer;

import com.hot6.backend.chat.model.ChatDto;
import com.hot6.backend.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageSaveConsumer {
    private final ChatRoomService chatRoomService;

    @KafkaListener(topics = "chat-messages", groupId = "chat-db-saver")
    public void saveToDatabase(ChatDto.ChatMessageDto message,
                               @Header("kafka_receivedMessageKey") String chatRoomId) {
        log.info("💾 [DB 저장 전용] roomId={}, message={}", chatRoomId, message.getContent());
        chatRoomService.saveSendMessage(message.getChatroomId(), message.getSenderIdx(), message);
    }
}
