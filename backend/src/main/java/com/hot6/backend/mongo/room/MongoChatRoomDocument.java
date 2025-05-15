package com.hot6.backend.mongo.room;

import com.hot6.backend.chat.model.ChatRoomUserMetaData;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_rooms")
public class MongoChatRoomDocument {
    @Id
    private Long idx;
    private String title;

    @Builder.Default
    private int maxParticipants = 100;

    private List<MongoParticipant> participants;
    private List<String> hashtags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MongoParticipant {
        private Long userId;
        private String nickname;
        private String profileImage;
        private boolean isAdmin;
        private ChatRoomUserMetaData metaData;
    }
}
