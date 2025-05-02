package com.hot6.backend.chat.model;

import com.hot6.backend.common.BaseEntity;
import com.hot6.backend.user.model.User;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DialectOverride;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE chat_room_participant SET is_deleted = true WHERE idx = ?")
@AllArgsConstructor
@Table(name = "chat_room_participant")
public class ChatRoomParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private Boolean isAdmin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "user_idx",
            foreignKey = @ForeignKey(name = "fk_chatroomparticipant_user", foreignKeyDefinition = "FOREIGN KEY (user_idx) REFERENCES user(idx) ON DELETE CASCADE")
    )
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "chat_room_idx",
            foreignKey = @ForeignKey(name = "fk_chatroomparticipant_chatroom", foreignKeyDefinition = "FOREIGN KEY (chat_room_idx) REFERENCES chat_room(idx) ON DELETE CASCADE")
    )
    private ChatRoom chatRoom;

    @OneToMany(mappedBy = "chatRoomParticipant")
    private List<Chat> chats = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "meta_data", columnDefinition = "json")
    private ChatRoomUserMetaData metaData;

    private boolean isDeleted = false;
}