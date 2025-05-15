package com.hot6.backend.mongo.message.repository;

import com.hot6.backend.mongo.message.model.MongoChatMessageDocument;

import java.util.List;

public interface MongoChatMessageRepositoryCustom {
    List<MongoChatMessageDocument> findWithPagination(
            Long roomId, Long firstJoinMessageId, Long lastMessageId, int size
    );
}