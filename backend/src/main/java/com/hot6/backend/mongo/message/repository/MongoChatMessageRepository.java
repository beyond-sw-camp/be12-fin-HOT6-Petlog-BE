package com.hot6.backend.mongo.message.repository;

import com.hot6.backend.mongo.message.model.MongoChatMessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoChatMessageRepository extends MongoRepository<MongoChatMessageDocument, Long>,
        MongoChatMessageRepositoryCustom{
    List<MongoChatMessageDocument> findByRoomIdAndIdxGreaterThanEqualOrderByIdxDesc(
            Long roomId,
            Long firstJoinMessageId,
            Pageable pageable
    );

    Optional<MongoChatMessageDocument> findTopByRoomIdOrderByIdxDesc(Long roomId);

    Optional<MongoChatMessageDocument> findTopByOrderByIdxDesc();
}