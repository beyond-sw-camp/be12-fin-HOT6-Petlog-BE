package com.hot6.backend.mongo;

import java.util.List;

public interface MongoChatMessageRepositoryCustom {
    List<MongoChatMessageDocument> findWithPagination(
            Long roomId, Long firstJoinMessageId, Long lastMessageId, int size
    );
}