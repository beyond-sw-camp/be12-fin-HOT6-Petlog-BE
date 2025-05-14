package com.hot6.backend.board.question.model;

import com.hot6.backend.board.hashtagQuestion.model.Hashtag_Question;
import com.hot6.backend.board.question.images.QuestionImage;
import com.hot6.backend.common.BaseEntity;
import com.hot6.backend.pet.model.Pet;
import com.hot6.backend.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class Question extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private String qTitle;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    private boolean selected;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Hashtag_Question> hashtagsList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", nullable = false)
    private User user;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<QuestionImage> questionImageList;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<Pet> petList;
}
