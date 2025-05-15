package com.hot6.backend.chat.service;

import com.hot6.backend.chat.model.Chat;
import com.hot6.backend.chat.model.ChatDto;
import com.hot6.backend.chat.model.ChatRoom;
import com.hot6.backend.chat.model.ChatRoomParticipant;
import com.hot6.backend.chat.repository.ChatMessageRepository;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public Slice<ChatDto.ChatElement> findChatMessages(
            ChatRoomParticipant chatRoomParticipant,
            Long lastMessageId,
            int size
    ) {
        Long roomId = chatRoomParticipant.getChatRoom().getIdx();
        Long firstJoinMessageId = chatRoomParticipant.getMetaData().getFirstJoinMessageId();

        // 💡 첫 참여 메시지 ID가 null일 경우 예외 처리
        if (firstJoinMessageId == null) {
            throw new BaseException(BaseResponseStatus.CHAT_ROOM_FIRST_JOIN_ID_NULL);
        }

        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "idx"));

        Slice<Chat> chats = chatMessageRepository.findSliceForScroll(roomId, firstJoinMessageId, lastMessageId, pageable);

        // 최신순으로 불러왔으니 클라이언트에 줄 땐 다시 시간순(ASC)으로 정렬
        List<ChatDto.ChatElement> content = chats.getContent().stream()
                .sorted(Comparator.comparing(Chat::getIdx))
                .map(ChatDto.ChatElement::from)
                .toList();

        return new SliceImpl<>(content, pageable, chats.hasNext());
    }

    @Transactional(readOnly = false)
    public ChatDto.ChatElement saveChatMessage(Chat chat) {
        return ChatDto.ChatElement.from(chatMessageRepository.save(chat));
    }

    @Transactional(readOnly = true)
    public Optional<Chat> findLatestChatByChatRoom(ChatRoom chatRoom) {
        return chatMessageRepository.findTopByChatRoomParticipant_ChatRoomOrderByIdxDesc(chatRoom.getIdx());
    }
}
