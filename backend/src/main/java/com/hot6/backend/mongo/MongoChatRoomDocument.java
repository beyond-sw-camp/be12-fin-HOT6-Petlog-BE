package com.hot6.backend.mongo;

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
    private int maxParticipants;

    private List<MongoParticipant> participants;
    private List<String> hashtags;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MongoParticipant {
        private Long userId;
        private String nickname;
        private String profileImage;
        private boolean isAdmin;
    }
}
