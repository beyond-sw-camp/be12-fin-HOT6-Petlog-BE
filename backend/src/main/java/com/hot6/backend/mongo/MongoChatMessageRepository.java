package com.hot6.backend.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoChatMessageRepository extends MongoRepository<MongoChatMessageDocument, Long>,
        MongoChatMessageRepositoryCustom{
    List<MongoChatMessageDocument> findByRoomIdAndIdxGreaterThanEqualOrderByIdxDesc(
            Long roomId,
            Long firstJoinMessageId,
            Pageable pageable
    );

}