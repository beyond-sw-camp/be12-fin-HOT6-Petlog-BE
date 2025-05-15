package com.hot6.backend.admin;

import com.hot6.backend.chat.model.ChatDto;
import com.hot6.backend.chat.service.ChatRoomService;
import com.hot6.backend.common.BaseResponse;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import com.hot6.backend.user.UserService;
import com.hot6.backend.user.model.User;
import com.hot6.backend.user.model.UserDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "ADMIN", description = "관리자 기능 API")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final ChatRoomService chatRoomService;

    // 삭제된 사용자 목록 조회 API
    @GetMapping("/deletedUsers")
    public ResponseEntity<BaseResponse<List<UserDto.DeletedUserResponse>>> getDeletedUsers() {
        List<User> deletedUsers = userService.getDeletedUsers();

        List<UserDto.DeletedUserResponse> result = deletedUsers.stream()
                .map(UserDto.DeletedUserResponse::from)
                .toList();

        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS, result));
    }

    @PutMapping("/restoreUser/{userId}")
    public ResponseEntity<String> restoreUser(@PathVariable Long userId) {
        try {
            adminService.restoreUser(userId);
            return ResponseEntity.ok("사용자가 복구되었습니다.");
        } catch (BaseException e) {
            if (e.getStatus() == BaseResponseStatus.USER_NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 복구에 실패했습니다.");
        }
    }
    @GetMapping("/chatroom")
    public List<ChatDto.ChatRoomListDto> getAdminChatRooms(@AuthenticationPrincipal User user) {

        Long userIdx = user.getIdx();

        return chatRoomService.getAdminChatRooms(userIdx);

    }
}
