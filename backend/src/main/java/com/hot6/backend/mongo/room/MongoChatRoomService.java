package com.hot6.backend.mongo.room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hot6.backend.chat.model.*;
import com.hot6.backend.chat.repository.ChatMessageRepository;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import com.hot6.backend.mongo.MongoSequenceGenerator;
import com.hot6.backend.mongo.message.repository.MongoChatMessageRepository;
import com.hot6.backend.mongo.model.dto.MongoDto;
import com.hot6.backend.mongo.message.model.MongoChatMessageDocument;
import com.hot6.backend.user.UserService;
import com.hot6.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MongoChatRoomService {
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final MongoChatRoomRepository mongoChatRoomRepository;
    private final MongoChatMessageRepository mongoChatMessageRepository;
    private final MongoSequenceGenerator mongoSequenceGenerator;
    private final MongoSequenceGenerator sequenceGenerator;
    private final MongoTemplate mongoTemplate;

    public void createChatRoom(ChatDto.CreateChatRoomRequest request, Long userIdx) {
        User userByIdx = userService.findUserByIdx(userIdx);

        MongoChatRoomDocument.MongoParticipant creator = MongoChatRoomDocument.MongoParticipant.builder()
                .userId(userIdx)
                .nickname(userByIdx.getNickname())
                .profileImage(userByIdx.getUserProfileImage())
                .isAdmin(true)
                .build();


        Long chatRoomIdx = sequenceGenerator.getNextSequence("chat_room");
        MongoChatMessageDocument savedMessage = mongoChatMessageRepository.save(
                MongoChatMessageDocument.builder()
                        .idx(sequenceGenerator.getNextSequence("chat_message_idx"))
                        .senderId(userByIdx.getIdx())
                        .senderNickname(userByIdx.getNickname())
                        .createdAt(LocalDateTime.now())
                        .type(ChatMessageType.TEXT)
                        .roomId(chatRoomIdx)
                        .message(userByIdx.getNickname() + " 님이 채팅방을 생성하셨습니다!")
                        .build()
        );

        ChatRoomUserMetaData metaData = ChatRoomUserMetaData.builder()
                .firstJoinMessageId(savedMessage.getIdx())
                .lastSeenMessageId(savedMessage.getIdx())
                .joinedAt(LocalDateTime.now())
                .isMuted(false)
                .notificationsEnabled(true)
                .build();

        creator.setMetaData(metaData);

        MongoChatRoomDocument chatRoom = MongoChatRoomDocument.builder()
                .idx(chatRoomIdx)  // 인덱스 생성 방식은 프로젝트 정책에 따라 다를 수 있음
                .title(request.getTitle())
                .maxParticipants(100)
                .hashtags(request.getHashtags())
                .participants(List.of(creator))
                .build();

        // 5. 저장
        mongoChatRoomRepository.save(chatRoom);
    }

    @Transactional
    public void join(User user, Long roomIdx) {
        User userByIdx = userService.findUserByIdx(user.getIdx());

        MongoChatRoomDocument chatRoom = mongoChatRoomRepository.findByIdx(roomIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));

        if (chatRoom.getMaxParticipants() <= chatRoom.getParticipants().size()) {
            throw new BaseException(BaseResponseStatus.MAX_PARTICIPANT_LIMIT);
        }

        // 🔹 메시지 기준 참여 시점 설정
        MongoChatMessageDocument mongoChatMessageDocument = mongoChatMessageRepository.findTopByRoomIdOrderByIdxDesc(roomIdx).orElseThrow();

        ChatRoomUserMetaData metaData = ChatRoomUserMetaData.builder()
                .firstJoinMessageId(mongoChatMessageDocument.getIdx())
                .lastSeenMessageId(mongoChatMessageDocument.getIdx())
                .joinedAt(LocalDateTime.now())
                .isMuted(false)
                .notificationsEnabled(true)
                .build();

        // 🔹 참가자 생성 및 추가
        MongoChatRoomDocument.MongoParticipant newParticipant = MongoChatRoomDocument.MongoParticipant.builder()
                .userId(user.getIdx())
                .nickname(userByIdx.getNickname())
                .profileImage(userByIdx.getUserProfileImage())
                .isAdmin(false)
                .metaData(metaData)
                .build();

        chatRoom.getParticipants().add(newParticipant);


        // ✅ MongoTemplate 사용해서 participants 배열에만 push
        Query query = new Query(Criteria.where("idx").is(chatRoom.getIdx()));
        Update update = new Update().push("participants", newParticipant);
        mongoTemplate.updateFirst(query, update, MongoChatRoomDocument.class);
    }


    @Transactional
    public void updateChatRoomInfo(User user, Long chatRoomIdx, ChatDto.UpdateChatRequest request) {
        MongoChatRoomDocument chatRoom = mongoChatRoomRepository.findByIdx(chatRoomIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));

        boolean isAdmin = chatRoom.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(user.getIdx()) && p.isAdmin());

        if (!isAdmin) {
            throw new BaseException(BaseResponseStatus.CHAT_ROOM_UPDATE_NO_PERMISSION);
        }

        Update update = new Update();
        boolean needUpdate = false;

        if (request.getTitle() != null) {
            update.set("title", request.getTitle());
            needUpdate = true;
        }

        if (request.getHashtags() != null) {
            update.set("hashtags", request.getHashtags());
            needUpdate = true;
        }

        if (needUpdate) {
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("idx").is(chatRoomIdx)),
                    update,
                    MongoChatRoomDocument.class
            );
        }
    }

    @Transactional
    public void leaveChatRoom(Long chatRoomIdx, Long userId) {
        Query query = Query.query(Criteria.where("idx").is(chatRoomIdx));
        Update update = new Update().pull("participants", Query.query(Criteria.where("userId").is(userId)));

        mongoTemplate.updateFirst(query, update, MongoChatRoomDocument.class);
    }

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
            MongoChatRoomDocument chatRoom,
            Long userIdx,
            Long lastMessageId,
            int size
    ) {
        MongoChatRoomDocument.MongoParticipant participant = chatRoom.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userIdx))
                .findFirst()
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_PARTICIPANT_NOT_FOUND));

        ChatRoomUserMetaData metaData = participant.getMetaData();
        if (metaData == null || metaData.getFirstJoinMessageId() == null) {
            throw new BaseException(BaseResponseStatus.CHAT_ROOM_FIRST_JOIN_ID_NULL);
        }

        Pageable pageable = PageRequest.of(0, size);

        List<MongoChatMessageDocument> raw;

        if (lastMessageId == null) {
            raw = mongoChatMessageRepository
                    .findByRoomIdAndIdxGreaterThanEqualOrderByIdxDesc(chatRoom.getIdx(),  metaData.getFirstJoinMessageId(), pageable);
        } else {
            raw = mongoChatMessageRepository.findWithPagination(chatRoom.getIdx(),  metaData.getFirstJoinMessageId(), lastMessageId, size);
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

    public ChatDto.ChatElement saveSendMessage(Long roomIdx, Long sender, ChatDto.ChatMessageDto chatMessageDto) {
        MongoChatRoomDocument chatRoom = mongoChatRoomRepository.findByIdx(roomIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));

        Optional<MongoChatRoomDocument.MongoParticipant> participantOpt = chatRoom.getParticipants().stream()
                .filter(p -> p.getUserId().equals(sender))
                .findFirst();

        MongoChatRoomDocument.MongoParticipant chatRoomParticipant = participantOpt.orElseThrow(
                () -> new BaseException(BaseResponseStatus.CHAT_ROOM_PARTICIPANT_NOT_FOUND)
        );


        // 2. 메시지 내용 가공 (💥 이 로직이 여기 들어감!)
        String messageContent;
        try {
            switch (chatMessageDto.getContent().getType()) {
                case "text" -> {
                    ChatDto.TextContent content = objectMapper.convertValue(chatMessageDto.getContent(), ChatDto.TextContent.class);
                    messageContent = content.getMessage();
                }
                case "pet" -> {
                    ChatDto.PetContent content = objectMapper.convertValue(chatMessageDto.getContent(), ChatDto.PetContent.class);
                    messageContent = objectMapper.writeValueAsString(content); // 예외 발생 가능
                }
                case "schedule" -> {
                    ChatDto.ScheduleContent content = objectMapper.convertValue(chatMessageDto.getContent(), ChatDto.ScheduleContent.class);
                    messageContent = objectMapper.writeValueAsString(content); // 예외 발생 가능
                }
                default -> throw new IllegalArgumentException("알 수 없는 타입: " + chatMessageDto.getContent().getType());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지 직렬화 중 오류 발생", e); // 로그 찍거나 예외 래핑 가능
        }

        long newIdx = mongoSequenceGenerator.getNextSequence("chat_message_idx");

        MongoChatMessageDocument chat = MongoChatMessageDocument.builder()
                .idx(newIdx)
                .roomId(roomIdx)
                .senderId(sender)
                .senderNickname(chatRoomParticipant.getNickname())
                .type(ChatMessageType.from(chatMessageDto.getContent().getType()))
                .message(messageContent) // 또는 dto.getMessage() 등
                .createdAt(LocalDateTime.now())
                .build();

        return MongoDto.toChatElement(mongoChatMessageRepository.save(chat));
    }
}
