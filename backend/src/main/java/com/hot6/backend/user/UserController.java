package com.hot6.backend.user;

import com.hot6.backend.common.BaseResponse;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.pet.S3Service;
import com.hot6.backend.pet.model.PetDto;
import com.hot6.backend.user.model.User;
import com.hot6.backend.user.model.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 기능 API")
public class UserController {
    private final UserService userService;
    private final S3Service s3Service;

    @Operation(summary = "일반/관리자 회원가입", description = "이메일, 비밀번호, 닉네임, 프로필 이미지로 회원가입 (관리자는 role 필드에 ADMIN 전달)")
    @PostMapping("/sign-up")
    public ResponseEntity<BaseResponse<UserDto.CreateResponse>> register(@RequestBody UserDto.CreateRequest userCreateRequest) {
        return ResponseEntity.ok(new BaseResponse(BaseResponseStatus.SUCCESS, userService.signup(userCreateRequest)));
    }

    @Operation(summary = "일반/관리자 로그인", description = "이메일, 비밀번호로 로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.LoginResponse.class)))
    })
    @PostMapping("/login")
    public UserDto.LoginResponse login(@RequestBody UserDto.LoginRequest loginRequest) {
        return UserDto.LoginResponse.builder()
                .accessToken("ok")
                .build();
    }

    @Operation(summary = "이메일 인증 완료", description = "UUID를 통해 이메일 인증 처리")
    @GetMapping("/verify-email")
    public ResponseEntity<BaseResponse<String>> verifyEmail(@RequestParam String uuid, HttpServletResponse response) {
        userService.verify(uuid, response);
        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS));
    }

    @Operation(summary = "로그인 체크", description = "JWT 토큰으로 로그인 체크")
    @GetMapping("/auth/check")
    public ResponseEntity<BaseResponse<UserDto.LoginCheckResponse>> checkLogin() {
        UserDto.LoginCheckResponse response = userService.checkLogin();
        return ResponseEntity.ok(new BaseResponse(BaseResponseStatus.SUCCESS, response));
    }


    @Operation(summary = "회원 프로필 조회", description = "특정 회원의 프로필 정보 및 펫 카드 목록 조회")
    @GetMapping("/{idx}/profile")
    public UserDto.UserProfileResponse getProfile(@PathVariable Long idx) {
        // UserService의 getProfile 메서드를 호출하여 유저 정보를 반환
        return userService.getProfile(idx);
    }

    @Operation(summary = "프로필 이미지 수정", description = "회원의 프로필 이미지를 수정")
    @PostMapping("/{idx}/profile")
    public ResponseEntity<String> updateProfile(@PathVariable Long idx,
                                                @RequestBody UserDto.UserProfileImageUpdateRequest request) {
        return ResponseEntity.ok("ok");
    }

    @Operation(summary = "비밀번호 수정", description = "회원이 현재 비밀번호를 입력하고 새 비밀번호로 변경")
    @PostMapping("/password")  // 이제 {idx}를 포함할 필요 없음
    public ResponseEntity<String> updatePassword(
            @RequestBody UserDto.PasswordUpdateRequest request,
            @AuthenticationPrincipal User user) {
        try {
            // user.getIdx()로 로그인된 유저의 userId를 직접 가져옵니다.
            userService.updatePassword(user.getIdx(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴 처리 (소셜 회원은 비밀번호 불필요)")
    @DeleteMapping("/{idx}")
    public ResponseEntity<String> delete(@PathVariable Long idx,
                                         @RequestBody UserDto.UserDeleteRequest request) {
        return ResponseEntity.ok("ok");
    }

    @Operation(summary = "회원 로그아웃", description = "현재 로그인된 계정에서 로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(HttpServletResponse response) {
        userService.logout(response);
        return ResponseEntity.ok(new BaseResponse(BaseResponseStatus.SUCCESS, "로그아웃"));
    }

    @PostMapping("/{userId}/profileImage")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("profileImage") MultipartFile profileImage) {

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String imagePath = s3Service.upload(profileImage, "users/" + System.currentTimeMillis() + profileImage.getOriginalFilename());

                // ✅ DB 업데이트 수행
                userService.updateProfileImage(userId, imagePath);

                return ResponseEntity.ok(Map.of("profileImageUrl", imagePath));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "이미지 업로드 실패: " + e.getMessage()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "파일이 비어 있습니다."));
        }
    }
}

