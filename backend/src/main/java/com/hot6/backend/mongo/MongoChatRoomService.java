package com.hot6.backend.mongo;

import com.hot6.backend.chat.model.ChatDto;
import com.hot6.backend.chat.model.ChatRoomParticipant;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import com.hot6.backend.mongo.dto.MongoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MongoChatRoomService {

    private final MongoChatRoomRepository mongoChatRoomRepository;
    private final MongoChatMessageRepository mongoChatMessageRepository;

    public Slice<ChatDto.ChatRoomListDto> getList(Long userId, Pageable pageable) {
        List<MongoChatRoomDocument> all = mongoChatRoomRepository.findAll();
        List<ChatDto.ChatRoomListDto> content = all.stream()
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .map(r -> MongoDto.toListDto(r, userId))
                .toList();

        boolean hasNext = all.size() > pageable.getOffset() + pageable.getPageSize();
        return new SliceImpl<>(content, pageable, hasNext);
    }

    public Slice<ChatDto.MyChatRoomListDto> findMyChatRooms(Long userId, Pageable pageable) {
        List<MongoChatRoomDocument> rooms = mongoChatRoomRepository.findByParticipantsUserId(userId);

        List<ChatDto.MyChatRoomListDto> content = rooms.stream()
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .map(r -> MongoDto.toMyListDto(r))
                .toList();

        boolean hasNext = rooms.size() > pageable.getOffset() + pageable.getPageSize();
        return new SliceImpl<>(content, pageable, hasNext);
    }

    public List<ChatDto.ChatRoomListDto> searchChatRoom(Long userId, String query, List<String> hashtags) {
        List<MongoChatRoomDocument> rooms;

        if (query != null && hashtags != null) {
            rooms = mongoChatRoomRepository.findByTitleContainingAndHashtagsIn(query, hashtags);
        } else if (query != null) {
            rooms = mongoChatRoomRepository.findByTitleContaining(query);
        } else if (hashtags != null) {
            rooms = mongoChatRoomRepository.findByHashtagsIn(hashtags);
        } else {
            rooms = mongoChatRoomRepository.findAll();
        }

        return rooms.stream()
                .map(r -> MongoDto.toListDto(r, userId))
                .toList();
    }

    public ChatDto.ChatRoomDetailInfo getChatRoomInfo(Long chatRoomId, Long userId) {
        MongoChatRoomDocument room = mongoChatRoomRepository.findByIdx(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found"));
        return MongoDto.toDetailDto(room, userId);
    }

    @Transactional(readOnly = true)
    public Slice<ChatDto.ChatElement> findChatMessages(
            ChatRoomParticipant chatRoomParticipant,
            Long lastMessageId,
            int size
    ) {
        Long roomId = chatRoomParticipant.getChatRoom().getIdx();
        Long firstJoinMessageId = chatRoomParticipant.getMetaData().getFirstJoinMessageId();

        if (firstJoinMessageId == null) {
            throw new BaseException(BaseResponseStatus.CHAT_ROOM_FIRST_JOIN_ID_NULL);
        }

        Pageable pageable = PageRequest.of(0, size);

        List<MongoChatMessageDocument> raw;

        if (lastMessageId == null) {
            raw = mongoChatMessageRepository
                    .findByRoomIdAndIdxGreaterThanEqualOrderByIdxDesc(roomId, firstJoinMessageId, pageable);
        } else {
            raw = mongoChatMessageRepository.findWithPagination(roomId, firstJoinMessageId, lastMessageId, size);
        }

        boolean hasNext = raw.size() > size;
        if (hasNext) {
            raw = raw.subList(0, size);
        }

        // idx 기준 ASC 정렬 후 DTO 변환
        List<ChatDto.ChatElement> content = raw.stream()
                .sorted(Comparator.comparing(MongoChatMessageDocument::getIdx))
                .map(chat -> MongoDto.toChatElement(chat))
                .toList();

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
