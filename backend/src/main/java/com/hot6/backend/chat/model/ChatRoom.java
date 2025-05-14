package com.hot6.backend.chat.model;

import com.hot6.backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Chat_Room")
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private String cTitle;

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;

    private int maxParticipants;
    private LocalDateTime startDateTime;
    private int currentParticipants;


    @BatchSize(size = 50)
    @OneToMany(mappedBy = "chatRoom")
    private Set<ChatRoomParticipant> participants = new HashSet<>();

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "chatRoom")
    private Set<ChatRoomHashtag> hashtags = new HashSet<>();

    public void updateInfo(String title){
        this.cTitle = title;
    }

    public void incrementCurrentParticipants() {
        this.currentParticipants++;
    }

    public void decrementCurrentParticipants() {
        this.currentParticipants--;
    }
}