package com.hot6.backend.mongo;

import com.hot6.backend.chat.model.ChatDto;
import com.hot6.backend.mongo.dto.MongoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MongoChatRoomService {

    private final MongoChatRoomRepository mongoChatRoomRepository;

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
}
