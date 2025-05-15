package com.hot6.backend.mongo.message.repository;

import com.hot6.backend.mongo.message.model.MongoChatMessageDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MongoChatMessageRepositoryImpl implements MongoChatMessageRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<MongoChatMessageDocument> findWithPagination(
            Long roomId, Long firstJoinMessageId, Long lastMessageId, int size
    ) {
        Query query = new Query();

        Criteria criteria = Criteria.where("roomId").is(roomId)
                .and("idx").gte(firstJoinMessageId);

        if (lastMessageId != null) {
            criteria = criteria.lt(lastMessageId); // 병합된 조건
        }

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "idx"));
        query.limit(size + 1); // hasNext 판단용

        return mongoTemplate.find(query, MongoChatMessageDocument.class);
    }
}