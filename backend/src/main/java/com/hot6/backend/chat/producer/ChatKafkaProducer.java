package com.hot6.backend.chat.producer;

import com.hot6.backend.chat.model.ChatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatKafkaProducer {
    private final KafkaTemplate<String, ChatDto.ChatMessageDto> kafkaTemplate;

    public void send(String chatRoomId, ChatDto.ChatMessageDto message) {
        kafkaTemplate.send("chat-messages", chatRoomId, message);
    }
}