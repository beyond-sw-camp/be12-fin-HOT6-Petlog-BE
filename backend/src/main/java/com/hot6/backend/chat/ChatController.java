package com.hot6.backend.chat;

import com.hot6.backend.chat.model.ChatDto;
import com.hot6.backend.chat.service.ChatRoomParticipantService;
import com.hot6.backend.chat.service.ChatRoomService;
import com.hot6.backend.common.BaseResponse;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "그룹 채팅 기능 API")
public class ChatController {
    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;

    @Operation(summary = "그룹 채팅방 생성", description = "채팅방 제목과 해시태그를 포함하여 새로운 채팅방을 생성합니다.")
    @PostMapping
    public ResponseEntity<String> createGroupChat(@RequestBody ChatDto.CreateChatRoomRequest request, @AuthenticationPrincipal User user) {
        log.info("user info {}", user.getIdx());
        chatRoomService.createChatRoom(request, user.getIdx());
        return ResponseEntity.ok("채팅방 생성 완료");
    }

    @Operation(summary = "그룹 채팅방 수정", description = "채팅방 제목과 해시태그를 수정합니다.")
    @PutMapping("/{chatRoomIdx}")
    public ResponseEntity<String> updateGroupChat(
            @PathVariable Long chatRoomIdx,
            @RequestBody ChatDto.UpdateChatRequest request) {
        return ResponseEntity.ok("채팅방 수정 완료");
    }

    @Operation(summary = "그룹 채팅방 삭제", description = "채팅방을 삭제합니다.")
    @DeleteMapping("/{chatRoomIdx}")
    public ResponseEntity<String> deleteGroupChat(
             @PathVariable Long chatRoomIdx) {
        return ResponseEntity.ok("채팅방 삭제 완료");
    }

    @Operation(summary = "채팅방 나가기", description = "사용자가 채팅방에서 나갑니다.")
    @DeleteMapping("/chatroom/{chatRoomIdx}/leave")
    public ResponseEntity<BaseResponse<String>> leaveChatRoom(
            @PathVariable Long chatRoomIdx,
            @AuthenticationPrincipal User user) {
        chatRoomParticipantService.leaveChatRoom(chatRoomIdx,user.getIdx());
        return ResponseEntity.ok(new BaseResponse(BaseResponseStatus.SUCCESS,"성공적으로 나가졌습니다."));
    }

    @Operation(summary = "전체 채팅방 목록 조회", description = "전체 그룹 채팅방 리스트를 조회합니다.")
    @GetMapping("/")
    public ResponseEntity<BaseResponse<Slice<ChatDto.ChatRoomListDto>>> getChatList(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Long userIdx = (user != null) ? user.getIdx() : null;
        return ResponseEntity.ok(new BaseResponse(BaseResponseStatus.SUCCESS, chatRoomService.getList(userIdx,pageable)));
    }

    @Operation(summary = "참여 중인 채팅방 목록 조회", description = "사용자가 현재 참여 중인 채팅방 목록을 조회합니다.")
    @GetMapping("/chatrooms/me")
    public ResponseEntity<BaseResponse<Slice<ChatDto.MyChatRoomListDto>>> getUserChatRooms(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10) Pageable pageable) {
//        return ResponseEntity.ok(new BaseResponse(BaseResponseStatus.SUCCESS, chatRoomService.getChatRoomByUserIdx(user.getIdx())));
        return ResponseEntity.ok(new BaseResponse(BaseResponseStatus.SUCCESS, chatRoomService.findMyChatRooms(user.getIdx(),pageable)));
    }

    @Operation(summary = "채팅방 검색", description = "채팅방 제목 또는 해시태그로 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<ChatDto.ChatRoomListDto>> searchChat(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> hashtags) {

        List<ChatDto.ChatRoomListDto> list = List.of(

        );
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "단일 채팅방의 정보 조회", description = "단일 채팅방의 정보를 조회합니다.(채팅방 이름, 해시 태그)")
    @GetMapping("/chatroom/{chatRoomIdx}")
    public ResponseEntity<BaseResponse<ChatDto.ChatRoomDetailInfo>> getChatRoomInfo(
            @PathVariable Long chatRoomIdx
    ) {
        return ResponseEntity.ok(new BaseResponse(BaseResponseStatus.SUCCESS,chatRoomService.getChatRoomInfo(chatRoomIdx)));
    }

    @Operation(summary = "채팅방 정보 조회 - 현재 참여한 유저", description = "현재 채팅방에 참여하고 있는 유저의 목록을 조회합니다.")
    @GetMapping("/chatroom/{chatRoomIdx}/users")
    public ResponseEntity<BaseResponse<Slice<ChatDto.ChatUserInfo>>> getChatRoomUsers(
            @PathVariable Long chatRoomIdx,
            @RequestParam(required = false) Long lastUserId,
            @RequestParam(defaultValue = "20") int size
    ) {
        Slice<ChatDto.ChatUserInfo> users = chatRoomParticipantService.getChatRoomInUsers(chatRoomIdx, lastUserId, size);
        return ResponseEntity.ok(new BaseResponse(BaseResponseStatus.SUCCESS,users));
    }

    @Operation(summary = "채팅 메시지 조회", description = "지정된 채팅방의 이전 메시지를 조회합니다.")
    @GetMapping("/chatroom/{chatRoomIdx}/chat")
    public ResponseEntity<BaseResponse<List<ChatDto.ChatElement>>> getChatMessages(
            @AuthenticationPrincipal User user,
            @PathVariable Long chatRoomIdx) {
        return ResponseEntity.ok(new BaseResponse(BaseResponseStatus.SUCCESS,chatRoomService.getChatMessages(chatRoomIdx,user.getIdx())));
    }

    @Operation(
            summary = "WebSocket 채팅 메시지 수신",
            description = """
        이 기능은 WebSocket 기반으로 구현되며, STOMP 프로토콜을 사용합니다.<br><br>
        - 연결 주소: `ws://{server}/ws/chat`<br>
        - 구독 주소: `/topic/chatroom/{chatRoomIdx}`<br>
        - 전송 주소: `/app/chat.sendMessage`<br>
        - 메시지 형식: JSON<br>
        - 실시간 메시지 전송 및 수신은 Swagger에서 테스트할 수 없습니다. <br><br>
        """
    )
    @GetMapping("/chatroom/ws-doc")
    public ResponseEntity<String> websocketDoc() {
        return ResponseEntity.ok("Swagger 문서용 WebSocket 설명");
    }
}
