package com.hot6.backend.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hot6.backend.chat.model.*;
import com.hot6.backend.chat.repository.ChatRoomRepository;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import com.hot6.backend.mongo.room.MongoChatRoomDocument;
import com.hot6.backend.mongo.room.MongoChatRoomRepository;
import com.hot6.backend.mongo.room.MongoChatRoomService;
import com.hot6.backend.pet.PetService;
import com.hot6.backend.pet.SharedSchedulePetService;
import com.hot6.backend.pet.model.Pet;
import com.hot6.backend.schedule.ScheduleService;
import com.hot6.backend.schedule.model.Schedule;
import com.hot6.backend.user.UserService;
import com.hot6.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {
    private final ObjectMapper objectMapper;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatRoomHashtagService chatRoomHashtagService;
    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final ScheduleService scheduleService;
    private final PetService petService;
    private final SharedSchedulePetService sharedSchedulePetService;
    private final MongoChatRoomService mongoChatRoomService;
    private final MongoChatRoomRepository mongoChatRoomRepository;

    @Transactional(readOnly = true)
    public Slice<ChatDto.ChatRoomListDto> getList(Long userIdx, Pageable pageable) {
        Slice<ChatRoom> chatRooms = chatRoomRepository.findAllWithSlice(pageable);
        //ChatInfo 에 정보 추가(isParticipating)
        return chatRooms.map(room -> ChatDto.ChatRoomListDto.from(room, userIdx));
    }

    @Transactional(readOnly = false)
    public void createChatRoom(ChatDto.CreateChatRoomRequest request, Long userIdx) {
        ChatRoom chatRoom = ChatRoom.builder()
                .cTitle(request.getTitle())
                .maxParticipants(10)
                .build();
        chatRoomRepository.save(chatRoom);
        List<ChatRoomHashtag> hashtags = new ArrayList<>();
        for(String hashtag : request.getHashtags()) {
            ChatRoomHashtag chatRoomHashtag = ChatRoomHashtag.builder()
                    .chatRoom(chatRoom)
                    .cTag(hashtag)
                    .build();
            hashtags.add(chatRoomHashtag);
        }
        chatRoomHashtagService.saveAll(hashtags);
        User findUser = userService.findUserByIdx(userIdx);
        chatRoomParticipantService.save(findUser, chatRoom);
    }

    @Transactional(readOnly = true)
    public Slice<ChatDto.MyChatRoomListDto> findMyChatRooms(Long userId, Pageable pageable) {
        List<Long> roomIds = chatRoomParticipantService.findChatRoomIdsByUserId(userId);
        if (roomIds.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        // Slice를 리턴하는 커스텀 레포지토리 메서드 사용
        Slice<ChatRoom> rooms = chatRoomRepository.findChatRoomsWithDetailsByIds(roomIds, pageable);

        return rooms.map(ChatDto.MyChatRoomListDto::from);
    }

    @Transactional(readOnly = true)
    public Slice<ChatDto.ChatElement> getChatMessages(Long chatRoomIdx, Long userIdx, Long lastMessageId,int size) {
        MongoChatRoomDocument chatRoom = mongoChatRoomRepository.findByIdx(chatRoomIdx).orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));
//        ChatRoomParticipant chatRoomParticipant = chatRoomParticipantService.findChatRoomParticipantOrThrow(chatRoomIdx, userIdx);
//        return chatMessageService.findChatMessages(chatRoomParticipant,lastMessageId,size);
        return mongoChatRoomService.findChatMessages(chatRoom,userIdx,lastMessageId,size);
    }

    @Transactional(readOnly = true)
    public ChatDto.ChatRoomDetailInfo getChatRoomInfo(Long chatRoomIdx,Long userIdx) {
        chatRoomParticipantService.findChatRoomParticipantOrThrow(chatRoomIdx, userIdx);
        ChatRoom chatRoom = chatRoomRepository.findWithParticipantsAndHashtagsById(chatRoomIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));

        return ChatDto.ChatRoomDetailInfo.from(chatRoom,userIdx);
    }

    @Transactional(readOnly = false)
    public ChatDto.ChatElement saveSendMessage(Long roomIdx, Long sender, ChatDto.ChatMessageDto chatMessageDto) {
        ChatRoom chatRoom = chatRoomRepository.findByIdx(roomIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));
        ChatRoomParticipant chatRoomParticipant = chatRoomParticipantService.findChatRoomParticipantOrThrow(chatRoom.getIdx(), sender);

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

        Chat chat = Chat.builder()
                .chatRoomParticipant(chatRoomParticipant)
                .type(ChatMessageType.from(chatMessageDto.getContent().getType()))
                .message(messageContent) // 또는 dto.getMessage() 등
                .build();

        return chatMessageService.saveChatMessage(chat);
    }

    @Transactional(readOnly = true)
    public List<ChatDto.ChatRoomScheduleElement> getChatRoomSchedule(Long chatRoomIdx) {
        return scheduleService.getALLScheduleByChatRoom(chatRoomIdx);
    }

    @Transactional(readOnly = false)
    public void createChatRoomSchedule(ChatDto.CreateChatRoomScheduleRequest dto, Long chatRoomIdx, User user) {
        ChatRoom chatRoom = chatRoomRepository.findByIdx(chatRoomIdx).orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));
        scheduleService.createChatRoomSchedule(dto, chatRoom, user);
    }

    @Transactional(readOnly = true)
    public ChatDto.ChatRoomScheduleDetailResponse getChatRoomScheduleDetail(Long chatRoomIdx, Long scheduleIdx ,User user) {
        ChatRoom chatRoom = chatRoomRepository.findByIdx(chatRoomIdx).orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));
        Schedule schedule = scheduleService.getSchedule(scheduleIdx);
        List<User> usersInChatRoomsSchedule = scheduleService.findChatRoomUsersParticipatingInSchedule(scheduleIdx);


        // 현재 사용자의 참여 여부
        boolean isParticipating = usersInChatRoomsSchedule.stream()
                .anyMatch(u -> u.getIdx().equals(user.getIdx()));

        List<Pet> usersPet = isParticipating == false ? petService.findByUser(user) : Collections.emptyList();

        // 응답 DTO 생성
        return ChatDto.ChatRoomScheduleDetailResponse.from(schedule,usersInChatRoomsSchedule,isParticipating,usersPet);
    }

    @Transactional(readOnly = false)
    public void participateChatRoomSchedule(Long chatRoomIdx, Long scheduleIdx, User user, ChatDto.ParticipateChatRoomSchedule dto) {
        chatRoomRepository.findByIdx(chatRoomIdx).orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));
        Schedule schedule = scheduleService.getSchedule(scheduleIdx);

        sharedSchedulePetService.saveAll(dto.getAnimalIds(), schedule);
    }

    @Transactional(readOnly = false)
    public void join(User user, Long roomIdx) {
        ChatRoom chatRoom = chatRoomRepository.findByIdx(roomIdx).orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));
        int curParticipants = chatRoomParticipantService.countByChatRoom(chatRoom);
        if(chatRoom.getMaxParticipants() == curParticipants) {
            throw new BaseException(BaseResponseStatus.MAX_PARTICIPANT_LIMIT);
        }
        chatRoomParticipantService.join(user, chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatDto.ChatRoomListDto> searchChatRoom(Long userIdx, String query, List<String> hashtags) {
        List<ChatRoom> chatRooms;

        if (query != null && !query.isBlank()) {
            chatRooms = chatRoomRepository.findByTitleWithParticipantsAndTags(query);
        } else {
            chatRooms = chatRoomRepository.findByTagsWithParticipants(hashtags);
        }

        return chatRooms.stream()
                .map(room -> ChatDto.ChatRoomListDto.from(room, userIdx))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public void updateChatRoomInfo(User user, Long chatRoomIdx, ChatDto.UpdateChatRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findByIdx(chatRoomIdx).orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));
        // 채팅방 참여자(isAdmin) == userIdx

        ChatRoomParticipant participant = chatRoomParticipantService.findByChatRoomAndUser(chatRoom, user);

        // 3. 관리자 여부 확인
        if (!participant.getIsAdmin()) {
            throw new BaseException(BaseResponseStatus.CHAT_ROOM_UPDATE_NO_PERMISSION);
        }

        chatRoom.updateInfo(request.getTitle());
        // 해시태그 변경...
        //입력값... 기존 해시태그랑 비교?
        //새로 생성된 것과 없어진것 찾아서 없어진것 삭제 , 새로 생성된건 추가...

        // 🔧 1. 기본 정보 업데이트
        chatRoom.updateInfo(request.getTitle());

        // 🔧 2. 기존 해시태그 가져오기
        List<ChatRoomHashtag> existingTags = chatRoomHashtagService.findByChatRoom(chatRoom);
        Set<String> existingTagNames = existingTags.stream()
                .map(ChatRoomHashtag::getCTag)
                .collect(Collectors.toSet());

        // 🔧 3. 클라이언트로부터 받은 새로운 해시태그 목록
        Set<String> newTagNames = new HashSet<>(request.getHashtags()); // 이미 # 제거된 상태

        // 🔧 4. 삭제 대상 찾기 (기존에는 있었는데, 새 요청에는 없는 것)
        List<ChatRoomHashtag> toRemove = existingTags.stream()
                .filter(tag -> !newTagNames.contains(tag.getCTag()))
                .collect(Collectors.toList());

        chatRoomHashtagService.deleteAll(toRemove);

        // 🔧 5. 추가 대상 찾기 (요청에는 있는데, 기존에는 없는 것)
        List<ChatRoomHashtag> toAdd = newTagNames.stream()
                .filter(tag -> !existingTagNames.contains(tag))
                .map(tag -> ChatRoomHashtag.builder()
                        .chatRoom(chatRoom)
                        .cTag(tag)
                        .build())
                .collect(Collectors.toList());

        chatRoomHashtagService.saveAll(toAdd);
    }
}
