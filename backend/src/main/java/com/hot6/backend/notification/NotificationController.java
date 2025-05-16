package com.hot6.backend.notification;

import com.hot6.backend.notification.model.NotificationDto;
import com.hot6.backend.schedule.model.Schedule;
import com.hot6.backend.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notification")
@Tag(name = "Notification", description = "실시간 알림 기능 API")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "알림 생성 및 실시간 전송", description = "스케줄과 연동된 알림을 생성하고 실시간으로 전송합니다.")
    @PostMapping("/create")
    public ResponseEntity<String> createAndSendNotification(
            @RequestBody NotificationDto.NotificationSendRequest request,@AuthenticationPrincipal User user
    ) {
        System.out.println("전송된 scheduleId: " + request.getScheduleId());
        notificationService.createNotification(request, user.getIdx());
        return ResponseEntity.ok("알림 생성 및 전송 완료");
    }

    @GetMapping("/user/{userId}")
    public List<NotificationDto.NotificationElement> getUserNotifications(@PathVariable Long userId, @AuthenticationPrincipal User user) {
        return notificationService.getNotificationsByUserId(user.getIdx());
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 표시합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long notificationId, @AuthenticationPrincipal User user) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("알림 읽음 처리 완료");
    }

    @Operation(summary = "알림 삭제", description = "알림 ID를 기반으로 사용자의 알림을 삭제합니다.")
    @DeleteMapping("/{notificationIdx}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationIdx, @AuthenticationPrincipal User user) {
        notificationService.deleteById(notificationIdx);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "읽지 않은 알림 개수 조회", description = "로그인한 사용자의 안 읽은 알림 개수만 반환합니다.")
    @GetMapping("/user/unreadCount")
    public ResponseEntity<Integer> getUnreadNotificationCount(@AuthenticationPrincipal User user) {
        int count = notificationService.countUnreadByReceiverId(user.getIdx());
        return ResponseEntity.ok(count);
    }
}

