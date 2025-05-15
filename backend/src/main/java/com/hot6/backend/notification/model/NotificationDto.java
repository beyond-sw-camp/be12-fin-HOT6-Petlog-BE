package com.hot6.backend.notification.model;

import com.hot6.backend.schedule.model.Schedule;
import com.hot6.backend.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NotificationDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "알림 항목")
    public static class NotificationElement {

        @Schema(description = "알림 ID", example = "10")
        private Long idx;

        @Schema(description = "알림 메시지", example = "병원 예약 1시간 전입니다.")
        private String message;

        @Schema(description = "알림 시간", example = "2025-04-07T11:00:00")
        private LocalDateTime sentAt;

        @Schema(description = "펫 ID", example = "멍쟈")
        private String petName;

        @Schema(description = "스케줄 ID", example = "101")
        private Long scheduleId;

        @Schema(description = "알림 읽음", example = "1")
        private boolean isRead;

        @Schema(description = "받는이", example = "1")
        private User receiver;

        // ✅ 엔티티 → DTO 변환 (정적 팩토리 메서드)
        public static NotificationElement from(Notification notification) {
            return NotificationElement.builder()
                    .idx(notification.getIdx())
                    .message(notification.getMessage())
                    .sentAt(notification.getSentAt())
                    .petName(notification.getSchedule().getPet().getName())
                    .scheduleId(notification.getSchedule().getIdx())
                    .isRead(notification.isRead())
                    .receiver(notification.getReceiver())
                    .build();
        }

        public Notification toEntity(Schedule schedule, User receiver) {
            Notification notification = new Notification();
            notification.setMessage(this.message);
            notification.setSentAt(this.sentAt != null ? this.sentAt : LocalDateTime.now());
            notification.setSchedule(schedule);
            notification.setReceiver(receiver); // ✅ 수신자 설정
            return notification;
        }
    }
    /**
     * ✅ 알림 생성 요청용 DTO (스케줄러 → 서비스 전달용)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "알림 전송 요청 DTO")
    public static class NotificationSendRequest {

        @Schema(description = "수신자 사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "스케줄 ID", example = "101")
        private Long scheduleId;

        @Schema(description = "알림 메시지", example = "산책 일정 30분 전입니다.")
        private String message;
    }
}
