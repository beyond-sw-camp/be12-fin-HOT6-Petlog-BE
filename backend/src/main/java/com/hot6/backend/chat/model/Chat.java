package com.hot6.backend.chat.model;

import com.hot6.backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Chat")
public class Chat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private String message;

    private Boolean cIsRead;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chat_room_parti_idx")
    private ChatRoomParticipant chatRoomParticipant;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ChatMessageType type;
}