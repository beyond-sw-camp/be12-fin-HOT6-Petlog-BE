package com.hot6.backend.board.question.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionListResponse {
    private List<QuestionDto.QuestionResponse> content;
    private int currentPage;
    private int totalPages;
    private int pageGroupStart;
    private int pageGroupEnd;
    private List<Integer> visiblePages;
}

