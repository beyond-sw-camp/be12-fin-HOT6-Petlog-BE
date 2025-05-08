package com.hot6.backend.mongo.dto;

import com.hot6.backend.chat.model.ChatDto;
import com.hot6.backend.mongo.MongoChatMessageDocument;
import com.hot6.backend.mongo.MongoChatRoomDocument;

public class MongoDto {

    public static ChatDto.ChatRoomListDto toListDto(MongoChatRoomDocument chatRoom, Long userIdx) {
        return ChatDto.ChatRoomListDto.builder()
                .idx(chatRoom.getIdx())
                .title(chatRoom.getTitle())
                .participants(chatRoom.getParticipants().size())
                .hashtags(chatRoom.getHashtags())
                .isParticipating(
                        userIdx != null && chatRoom.getParticipants().stream()
                                .anyMatch(p -> p.getUserId().equals(userIdx))
                )
                .build();
    }

    public static ChatDto.MyChatRoomListDto toMyListDto(MongoChatRoomDocument chatRoom) {
        return ChatDto.MyChatRoomListDto.builder()
                .idx(chatRoom.getIdx())
                .title(chatRoom.getTitle())
                .participants(chatRoom.getParticipants().size())
                .hashtags(chatRoom.getHashtags())
                .isParticipating(true)
                .build();
    }

    public static ChatDto.ChatRoomDetailInfo toDetailDto(MongoChatRoomDocument chatRoom, Long userIdx) {
        return ChatDto.ChatRoomDetailInfo.builder()
                .idx(chatRoom.getIdx())
                .title(chatRoom.getTitle())
                .participants(chatRoom.getParticipants().size())
                .hashtags(chatRoom.getHashtags())
                .isAdmin(
                        chatRoom.getParticipants().stream()
                                .anyMatch(p -> p.getUserId().equals(userIdx) && p.isAdmin())
                )
                .build();
    }

    public static ChatDto.ChatElement toChatElement(MongoChatMessageDocument doc) {
        return ChatDto.ChatElement.builder()
                .idx(doc.getIdx())
                .content(doc.getMessage())
                .type(doc.getType())
                .senderIdx(doc.getSenderId())
                .nickname(doc.getSenderNickname())
                .createdAt(doc.getCreatedAt().toString())
                .build();
    }
}
