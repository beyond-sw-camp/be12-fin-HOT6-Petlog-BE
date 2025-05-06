package com.hot6.backend.chat.consumer;

import com.hot6.backend.chat.model.ChatDto;
import com.hot6.backend.chat.service.ChatMessageService;
import com.hot6.backend.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatKafkaConsumer {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;

    @KafkaListener(topics = "chat-messages", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ChatDto.ChatMessageDto message,
                       @Header("kafka_receivedMessageKey") String chatRoomId) {

        log.info("📥 [Kafka 메시지 수신] roomId={}, message={}", chatRoomId, message.getContent());
        chatRoomService.saveSendMessage(message.getChatroomId(), message.getSenderIdx(), message);
        // WebSocket 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, message);
    }
}