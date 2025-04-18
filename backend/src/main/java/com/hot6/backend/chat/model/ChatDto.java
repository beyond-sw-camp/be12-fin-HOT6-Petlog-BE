package com.hot6.backend.chat.model;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

public class ChatDto {
    @Getter
    @Schema(description = "채팅방 생성 요청 DTO")
    public static class CreateChatRoomRequest {
        @Schema(description = "채팅방 제목", example = "햄스터 친구 구해요")
        private String title;

        @Schema(description = "채팅방 해시태그 목록", example = "[\"#햄스터\", \"#김포\", \"#친구\"]")
        private List<String> hashtags;
    }

    @Getter
    @Schema(description = "채팅방 수정 요청 DTO")
    public static class UpdateChatRequest {
        @Schema(description = "변경할 채팅방 제목", example = "햄스터 사육 정보 공유방")
        private String title;

        @Schema(description = "수정할 해시태그 목록", example = "[\"#햄스터\", \"#정보\"]")
        private List<String> hashtags;
    }

    @Getter
    @Builder
    @Schema(description = "채팅방 정보 응답 DTO")
    public static class ChatRoomListDto {
        @Schema(description = "채팅방 idx", example = "1")
        public Long idx;

        @Schema(description = "채팅방 제목", example = "햄스터 친구 구해요")
        public String title;

        @Schema(description = "채팅방 해시태그", example = "[\"#햄스터\", \"#친구\"]")
        public List<String> hashtags;

        @Schema(description = "채팅방 참여 인원수", example = "6")
        public int participants;

        @Schema(description = "참여 여부", example = "true")
        public Boolean isParticipating;

        public static ChatRoomListDto from(ChatRoom chatRoom, Long userIdx) {
            return ChatRoomListDto.builder()
                    .idx(chatRoom.getIdx())
                    .title(chatRoom.getCTitle())
                    .participants(chatRoom.getParticipants().size())
                    .hashtags(chatRoom.getHashtags().stream().map(ChatRoomHashtag::getCTag).collect(Collectors.toList()))
                    .isParticipating(
                            userIdx != null && chatRoom.getParticipants().stream()
                            .anyMatch(participant -> participant.getUser().getIdx().equals(userIdx)))
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "채팅방 정보 응답 DTO")
    public static class MyChatRoomListDto {
        @Schema(description = "채팅방 idx", example = "1")
        public Long idx;

        @Schema(description = "채팅방 제목", example = "햄스터 친구 구해요")
        public String title;

        @Schema(description = "채팅방 해시태그", example = "[\"#햄스터\", \"#친구\"]")
        public List<String> hashtags;

        @Schema(description = "채팅방 참여 인원수", example = "6")
        public int participants;

        @Schema(description = "참여 여부", example = "true")
        public Boolean isParticipating;

        public static MyChatRoomListDto from(ChatRoom chatRoom) {
            return MyChatRoomListDto.builder()
                    .idx(chatRoom.getIdx())
                    .title(chatRoom.getCTitle())
                    .participants(chatRoom.getParticipants().size())
                    .hashtags(chatRoom.getHashtags().stream().map(ChatRoomHashtag::getCTag).collect(Collectors.toList()))
                    .isParticipating(true)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "채팅방 정보 응답 DTO")
    public static class ChatRoomDetailInfo {
        @Schema(description = "채팅방 idx", example = "1")
        public Long idx;

        @Schema(description = "채팅방 제목", example = "햄스터 친구 구해요")
        public String title;

        @Schema(description = "채팅방 해시태그", example = "[\"#햄스터\", \"#친구\"]")
        public List<String> hashtags;

        @Schema(description = "채팅방 참여 인원수", example = "6")
        public int participants;

        public static ChatRoomDetailInfo from(ChatRoom chatRoom) {
            return ChatRoomDetailInfo.builder()
                    .idx(chatRoom.getIdx())
                    .title(chatRoom.getCTitle())
                    .participants(chatRoom.getParticipants().size())
                    .hashtags(chatRoom.getHashtags().stream().map(ChatRoomHashtag::getCTag).collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "채팅 메시지 응답 DTO")
    public static class ChatElement {
        @Schema(description = "채팅 고유 idx", example = "1")
        public Long idx;

        @Schema(description = "채팅 메시지 내용", example = "안녕하세요~")
        public String message;

        @Schema(description = "보낸 사용자 idx", example = "1")
        public Long senderIdx;

        @Schema(description = "보낸 사용자 닉네임", example = "hamster_lover")
        public String nickname;

        @Schema(description = "메시지 타입", example = "text")
        private ChatMessageType type;

        @Schema(description = "보낸 시간", example = "2025-04-07T12:34:56")
        public String createdAt;


        public static ChatElement from(Chat chat) {
            return ChatElement.builder()
                    .idx(chat.getIdx())
                    .senderIdx(chat.getChatRoomParticipant().getUser().getIdx())
                    .nickname(chat.getChatRoomParticipant().getUser().getNickname())
                    .createdAt(chat.getCreatedAt().toString())
                    .message(chat.getMessage())
                    .type(chat.getType())
                    .build();
        }
    }

    @Getter
    @Schema(description = "채팅방 검색 요청 DTO (POST 방식용)")
    public static class ChatSearchRequest {
        @Schema(description = "검색어", example = "햄스터")
        public String query;

        @Schema(description = "검색할 해시태그 목록", example = "[\"#햄스터\"]")
        public List<String> hashtags;
    }

    @Getter
    @Schema(description = "채팅 메시지 전송 요청 DTO")
    public static class CreateChatRequest {
        @Schema(description = "채팅 메시지", example = "반가워요!")
        public String message;

        @Schema(description = "보낸 사용자 닉네임", example = "user01")
        public String nickname;

        @Schema(description = "채팅방 idx", example = "1")
        public Long chatRoomIdx;

        @Schema(description = "보낸 시간", example = "2025-04-07T12:00:00")
        public String createdAt;
    }

    @Getter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageDto {
        private Long chatroomId;
        private String type;        // "text", "image", "file" 등
        private String text;        // 본문 내용 or Base64 or URL
        private String timestamp;   // ISO String (정렬용, 표시용)
    }

    @Getter
    public static class ChatUserInfo {
        @Schema(description = "채팅방 idx", example = "1")
        public Long idx;

        @Schema(description = "채팅방에 참여한 유저 닉네임", example = "User1")
        public String userName;

        @Schema(description = "채팅방에 참여한 유저의 imageUrl", example = "User1")
        public String imageUrl;

        @QueryProjection
        public ChatUserInfo(Long idx, String userName, String imageUrl) {
            this.idx = idx;
            this.userName = userName;
            this.imageUrl = imageUrl;
        }
    }
}
