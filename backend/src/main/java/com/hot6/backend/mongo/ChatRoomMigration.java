package com.hot6.backend.mongo;

import com.hot6.backend.chat.model.ChatRoom;
import com.hot6.backend.chat.model.ChatRoomHashtag;
import com.hot6.backend.chat.repository.ChatRoomRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

//@Component
@RequiredArgsConstructor
public class ChatRoomMigration {

    private final ChatRoomRepository chatRoomRepository;
    private final MongoChatRoomRepository mongoChatRoomRepository;

    @Transactional(readOnly = true)
//    @PostConstruct
    public void migrate() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllWithParticipantsAndHashtags();

        List<MongoChatRoomDocument> docs = chatRooms.stream().map(room -> {
            List<MongoChatRoomDocument.MongoParticipant> participants = room.getParticipants().stream()
                    .filter(p -> !p.isDeleted())
                    .map(p -> new MongoChatRoomDocument.MongoParticipant(
                            p.getUser().getIdx(),
                            p.getUser().getNickname(),
                            p.getUser().getUserProfileImage(),
                            p.getIsAdmin()
                    ))
                    .collect(Collectors.toList());

            List<String> hashtags = room.getHashtags().stream()
                    .map(ChatRoomHashtag::getCTag)
                    .collect(Collectors.toList());

            return MongoChatRoomDocument.builder()
                    .idx(room.getIdx())
                    .title(room.getCTitle())
                    .maxParticipants(room.getMaxParticipants())
                    .participants(participants)
                    .hashtags(hashtags)
                    .build();
        }).toList();

        mongoChatRoomRepository.saveAll(docs);
    }
}
