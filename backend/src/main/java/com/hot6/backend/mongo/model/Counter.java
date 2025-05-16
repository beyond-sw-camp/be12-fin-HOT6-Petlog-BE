package com.hot6.backend.mongo.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "counters")
public class Counter {

    @Id
    private String id; // 시퀀스 이름 (ex: "chat_message_idx")
    private long seq;  // 현재 시퀀스 값
}
