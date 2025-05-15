package com.hot6.backend.schedule.model;

import com.hot6.backend.category.model.Category;
import com.hot6.backend.pet.model.Pet;
import com.hot6.backend.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class ScheduleDto {
    @Getter
    @Builder
    @Schema(description = "월간 일정 응답 DTO")
    public static class MonthlyScheduleResponse {
        @Schema(description = "월간 일정 리스트", implementation = SimpleSchedule.class)
        private List<SimpleSchedule> schedule;
    }

    @Getter
    @Builder
    @Schema(description = "일정 정보 요약 DTO")
    public static class SimpleSchedule {
        @Schema(description = "일정 idx", example = "1")
        private Long idx;

        @Schema(description = "일정 제목", example = "병원 방문")
        private String title;

        @Schema(description = "일정 시작 날짜", example = "2025-04-10T10:00:00")
        private LocalDateTime startAt;

        @Schema(description = "일정 끝나는 날짜", example = "2025-04-10T10:00:00")
        private LocalDateTime endAt;

        @Schema(description = "카테고리 색상", example = "#00C9CD")
        private String color;

        @Schema(description = "카테고리 이름", example = "병원")
        private String categoryName;

        @Schema(description = "펫 이름", example = "콩이")
        private String petName;

        @Schema(description = "채팅방 연동 여부", example = "false")
        private boolean fromChat;

        public static SimpleSchedule from(Schedule schedule, Category category) {
            return SimpleSchedule.builder()
                    .idx(schedule.getIdx())
                    .title(schedule.getSTitle())
                    .startAt(schedule.getStartAt())
                    .endAt(schedule.getEndAt())
                    .color(category.getColor())
                    .categoryName(category.getName())
                    .fromChat(false)  // [TODO]: 채팅방 연동 확인 후 수정
                    .petName(schedule.getPet().getName())
                    .build();
        }
    }


    @Getter  @Builder
    public static class ScheduleDetail {
        @Schema(description = "일정 고유 ID", example = "101")
        private Long idx;
        @Schema(description = "일정 제목", example = "예방접종")
        private String title;
        @Schema(description = "일정 메모", example = "병원에서 강아지 예방접종 받기")
        private String memo;

        @Schema(description = "반복 여부", example = "true")
        private boolean recurring;
        @Schema(description = "반복 주기", example = "주")
        private String repeatCycle;
        @Schema(description = "반복 수", example = "1")
        private int repeatCount;
        @Schema(description = "반복 종료 날짜", example = "2025-04-24")
        private LocalDate repeatEndAt;

        @Schema(description = "장소 이름", example = "서울 동물병원")
        private String placeName;

        @Schema(description = "장소 id", example = "1234")
        private String placeId;


        @Schema(description = "일정 시작 시간", example = "2025-04-10T10:00:00")
        private LocalDateTime startAt;
        @Schema(description = "일정 종료 시간", example = "2025-04-10T11:00:00")
        private LocalDateTime endAt;

        @Schema(description = "카테고리 색상", example = "#00C9CD")
        private String color;
        @Schema(description = "카테고리 이름", example = "병원")
        private String categoryName;

        @Schema(description = "펫 고유 ID",example = "1")
        private Long petIdx;

        @Schema(description = "펫 이름", example = "콩이")
        private String petName;

        public static ScheduleDetail from(Schedule schedule, Category category) {
            return ScheduleDetail.builder()
                    .idx(schedule.getIdx())
                    .title(schedule.getSTitle())
                    .memo(schedule.getSMemo())
                    .recurring(schedule.isRecurring())
                    .repeatCount(schedule.getRepeatCount())
                    .repeatCycle(schedule.getRepeatCycle())
                    .repeatEndAt(schedule.getRepeatEndAt())
                    .placeId(schedule.getPlaceId())
                    .placeName(schedule.getPlaceName())
                    .startAt(schedule.getStartAt())
                    .endAt(schedule.getEndAt())
                    .color(category.getColor())
                    .categoryName(category.getName())
                    .petIdx(schedule.getPet().getIdx())
                    .petName(schedule.getPet().getName())
                    .build();
        }
    }

    @Getter
    @Schema(description = "일정 생성 요청 DTO")
    public static class ScheduleCreateRequest {
        @Schema(description = "일정 제목", example = "예방접종")
        private String title;

        @Schema(description = "장소 이름", example = "서울 동물병원")
        private String placeName;

        @Schema(description = "장소 id", example = "1234")
        private String placeId;

        @Schema(description = "메모", example = "병원에서 예방접종")
        private String memo;

        @Schema(description = "카테고리 ID", example = "1")
        private Long categoryIdx;

        @Schema(description = "일정 시작 시간", example = "2025-04-10T10:00:00")
        private LocalDateTime startAt;

        @Schema(description = "일정 종료 시간", example = "2025-04-10T11:00:00")
        private LocalDateTime endAt;

        @Schema(description = "반복 여부", example = "true")
        private boolean recurring;

        @Schema(description = "반복 주기", example = "주")
        private String repeatCycle;

        @Schema(description = "반복 수", example = "1")
        private int repeatCount;

        @Schema(description = "반복 종료 날짜", example = "2025-04-24")
        private LocalDate repeatEndAt;

        public Schedule toEntity(Category category, Pet pet) {
            return Schedule.builder()
                    .sTitle(title)
                    .sMemo(memo)
                    .categoryIdx(category.getIdx())
                    .startAt(startAt)
                    .endAt(endAt)
                    .recurring(recurring)
                    .repeatCycle(repeatCycle)
                    .placeName(placeName)
                    .placeId(placeId)
                    .repeatCount(repeatCount)
                    .repeatEndAt(repeatEndAt)
                    .pet(pet)
                    .build();
        }
    }

    @Getter
    @Schema(description = "일정 수정 요청 DTO")
    public static class ScheduleUpdateRequest {
        @Schema(description = "일정 제목", example = "예방접종")
        private String title;

        @Schema(description = "장소 이름", example = "서울 동물병원")
        private String placeName;

        @Schema(description = "장소 id", example = "1234")
        private String placeId;

        @Schema(description = "메모", example = "병원에서 예방접종")
        private String memo;

        @Schema(description = "기록 날짜 (yyyy-MM-dd 형식)", example = "2025-04-02")
        private String date;

        @Schema(description = "카테고리 ID", example = "1")
        private Long categoryIdx;

        @Schema(description = "일정 시작 시간", example = "10:00")
        private LocalDateTime startAt;

        @Schema(description = "일정 종료 시간", example = "11:00")
        private LocalDateTime endAt;
    }


    @Schema(description = "일정 공유 요청 DTO")
    @Getter
    public static class ScheduleShareRequest {
        @Schema(description = "공유할 일정 ID", example = "101")
        private Long scheduleId;

        @Schema(description = "공유 대상 타입 (user | group)", example = "user")
        private String targetType;

        @Schema(description = "공유 대상 ID (사용자 ID 또는 그룹 ID)", example = "55")
        private Long targetId;
    }

}
