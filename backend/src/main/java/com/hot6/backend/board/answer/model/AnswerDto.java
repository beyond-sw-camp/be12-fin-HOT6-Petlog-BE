package com.hot6.backend.board.answer.model;

import com.hot6.backend.board.answer.images.AnswerImage;
import com.hot6.backend.user.model.UserType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

public class AnswerDto {

    @Getter
    @Setter
    public static class AnswerRequest {
        private Long questionIdx;
        private String content;
        private List<String> originalImageUrls;
    }

    @Getter
    @Builder
    public static class AnswerResponse {
        private Long questionIdx;
        private Long idx;
        private String content;
        private boolean selected;
        private LocalDate createdAt;
        private String writer;
        private List<String> imageUrls;
        private String profileImageUrl;
        private UserType userType;

        public static AnswerResponse from(Answer answer) {
            boolean isAi = answer.getUser().getUserType() == UserType.AI;

            return AnswerResponse.builder()
                    .questionIdx(answer.getQuestion().getIdx())
                    .idx(answer.getIdx())
                    .content(answer.getContent())
                    .selected(answer.isSelected())
                    .createdAt(LocalDate.from(answer.getCreatedAt()))
                    .writer(isAi ? "ChatGPS" : answer.getUser().getNickname())
                    .imageUrls(
                            answer.getAnswerImageList() != null
                                    ? answer.getAnswerImageList().stream()
                                    .map(AnswerImage::getUrl)
                                    .toList()
                                    : List.of()
                    )
                    .profileImageUrl(
                            isAi
                                    ? "/src/assets/icons/chatGPS.png"
                                    : answer.getUser().getUserProfileImage()
                    )
                    .userType(answer.getUser().getUserType())
                    .build();
        }
    }
}
