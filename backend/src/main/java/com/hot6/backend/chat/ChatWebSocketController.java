package com.hot6.backend.chat;

import com.hot6.backend.chat.model.ChatDto;
import com.hot6.backend.chat.producer.ChatKafkaProducer;
import com.hot6.backend.chat.service.ChatRoomService;
import com.hot6.backend.mongo.room.MongoChatRoomService;
import com.hot6.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final SimpMessagingTemplate simp;
    private final ChatRoomService chatRoomService;
    private final MongoChatRoomService mongoChatRoomService;
    private final ChatKafkaProducer kafkaProducer;

    @MessageMapping("/chat/{roomIdx}")
    public void sendMessage(@DestinationVariable Long roomIdx,
                            @Payload ChatDto.ChatMessageDto dto,
                            Principal principal
                            ) {
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        log.info("💬 [WebSocket 메시지 수신]");
        log.info("📌 roomIdx: {}", roomIdx);
        log.info("👤 sender: {} (user idx: {})", user.getNickname(), user.getIdx());
        log.info("✉️ payload: {}", dto);

//        simp.convertAndSend("/topic/chat/" + roomIdx, chatRoomService.saveSendMessage(roomIdx, user.getIdx(), dto));
//         simp.convertAndSend("/topic/chat/" + roomIdx, mongoChatRoomService.saveSendMessage(roomIdx, user.getIdx(), dto));
        // sender 정보 채워 넣기
        dto.setSenderIdx(user.getIdx());
        dto.setSender(user.getNickname());

        kafkaProducer.send(String.valueOf(roomIdx), dto);
    }
}
