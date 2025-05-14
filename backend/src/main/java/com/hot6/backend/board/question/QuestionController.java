package com.hot6.backend.board.question;

import com.hot6.backend.board.question.model.QuestionDto;
import com.hot6.backend.board.question.model.QuestionListResponse;
import com.hot6.backend.common.BaseResponse;
import com.hot6.backend.common.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/question")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<BaseResponse<String>> create(
            @RequestPart("question") QuestionDto.QuestionRequest dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {
        questionService.create(dto, images);
        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS));
    }

    @PutMapping(value = "/update/{idx}", consumes = {"multipart/form-data"})
    public ResponseEntity<BaseResponse<Void>> update(
            @PathVariable Long idx,
            @RequestPart("question") QuestionDto.QuestionRequest dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {
        questionService.update(idx, dto, images);
        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS));
    }

    @GetMapping("/list")
    public BaseResponse<QuestionListResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return new BaseResponse<>(questionService.list(page, size));
    }

    @GetMapping("/search")
    public BaseResponse<QuestionListResponse> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return new BaseResponse<>(questionService.search(keyword, page, size));
    }


    @GetMapping("/read/{idx}")
    public ResponseEntity<BaseResponse<QuestionDto.QuestionResponse>> read(@PathVariable Long idx) {
        QuestionDto.QuestionResponse response = questionService.read(idx);
        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS, response));
    }

    @DeleteMapping("/delete/{idx}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long idx) {
        questionService.delete(idx);
        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS));
    }

    @GetMapping("/list/user/{userId}")
    public ResponseEntity<List<QuestionDto.UserQuestionResponse>> getUserQuestions(@PathVariable Long userId) {
        return ResponseEntity.ok(questionService.findUserQuestions(userId));
    }
}
