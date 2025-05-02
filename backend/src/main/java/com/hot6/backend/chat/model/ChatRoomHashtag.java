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
@Table(name = "Chat_Room_Hashtag")
public class ChatRoomHashtag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private String cTag;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "chat_room_idx",
            foreignKey = @ForeignKey(name = "fk_chatroomhashtag_chatroom", foreignKeyDefinition = "FOREIGN KEY (chat_room_idx) REFERENCES chat_room(idx) ON DELETE CASCADE")
    )
    private ChatRoom chatRoom;
}
