package com.hot6.backend.record;

import com.hot6.backend.common.BaseResponse;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.record.model.DailyRecordDto;
import com.hot6.backend.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/daily-record")
@Tag(name = "daily-record", description = "기록(체중, 수면 등) 관리 API")
public class DailyRecordController {
    private final DailyRecordService dailyRecordService;

    @Operation(summary = "일일 기록 생성", description = "하루의 특정 반려 동물의 기록을 작성합니다.")
    @PostMapping("/pet/{petIdx}")
    public ResponseEntity<BaseResponse<String>> createDailyRecord(
            @RequestBody DailyRecordDto.RecordCreateRequest request,
            @PathVariable Long petIdx) {

        dailyRecordService.createDailyRecord(petIdx, request);
        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS));
    }

    @Operation(summary = "날짜별 기록 전체 조회", description = "특정 날짜의 반려동물 전체 기록을 조회합니다.")
    @GetMapping("/pet/{petIdx}/date/{year}/{month}/{day}")
    public ResponseEntity<BaseResponse<List<DailyRecordDto.SimpleDailyRecord>>> getRecordsByPetAndDate(
            @PathVariable Long petIdx,
            @PathVariable Integer year,
            @PathVariable Integer month,
            @PathVariable Integer day
    ) {
        List<DailyRecordDto.SimpleDailyRecord> list = dailyRecordService.getRecordsByPetAndDate(petIdx, year, month, day);

        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS, list));
    }

    @Operation(summary = "특정 반려동물의 날짜별 기록 조회", description = "특정 날짜의 특정 반려동물 기록을 조회합니다.")
    @GetMapping("/date/{year}/{month}/{day}")
    public ResponseEntity<BaseResponse<List<DailyRecordDto.SimpleDailyRecord>>> getRecordsByDate(
            @PathVariable Integer year,
            @PathVariable Integer month,
            @PathVariable Integer day,
            @AuthenticationPrincipal User user
    ) {
        List<DailyRecordDto.SimpleDailyRecord> list = dailyRecordService.getRecordsByDate(user.getIdx(), year, month, day);

        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS, list));
    }


    @Operation(summary = "기록 상세 조회", description = "특정 기록의 상세 정보를 조회합니다.")
    @GetMapping("/{recordIdx}")
    public ResponseEntity<BaseResponse<DailyRecordDto.RecordDetail>> getRecordDetail(
            @PathVariable Long recordIdx
    ) {
        DailyRecordDto.RecordDetail record = dailyRecordService.getRecordDetail(recordIdx);
        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS, record));
    }

    @Operation(summary = "기록 수정", description = "기존의 기록을 수정합니다.")
    @PutMapping("/{recordIdx}")
    public ResponseEntity<String> updateRecord(
            @PathVariable Long recordIdx,
            @RequestBody DailyRecordDto.RecordCreateRequest request
    ) {
        return ResponseEntity.ok("기록 수정 완료");
    }

    @Operation(summary = "기록 삭제", description = "기존 기록을 삭제합니다.")
    @DeleteMapping("/{recordIdx}")
    public ResponseEntity<String> deleteRecord(
            @PathVariable Long recordIdx
    ) {
        return ResponseEntity.ok("기록 삭제 완료");
    }

}
