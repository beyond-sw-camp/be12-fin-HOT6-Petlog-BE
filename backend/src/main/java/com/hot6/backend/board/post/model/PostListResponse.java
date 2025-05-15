package com.hot6.backend.board.post.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostListResponse {
    private List<PostDto.PostResponse> content;
    private int currentPage;
    private int totalPages;
    private int pageGroupStart;
    private int pageGroupEnd;
    private List<Integer> visiblePages;
}
