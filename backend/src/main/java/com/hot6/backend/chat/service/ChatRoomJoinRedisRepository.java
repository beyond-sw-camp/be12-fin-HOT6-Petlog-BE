package com.hot6.backend.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class ChatRoomJoinRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    // 사용자 참여 여부 확인
    public boolean sIsMember(String key, String userId) {
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
        return isMember != null && isMember;
    }

    // 사용자 참여 등록
    public void sAdd(String key, String userId) {
        redisTemplate.opsForSet().add(key, userId);
    }

    // 참여자 수 반환
    public long sCard(String key) {
        Long size = redisTemplate.opsForSet().size(key);
        return size != null ? size : 0;
    }

    // 참여 정보 삭제 (옵션)
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // TTL 설정 (선택사항)
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    // 큐에 사용자 입장 요청 추가
    public void enqueueJoinRequest(String key, String userId) {
        redisTemplate.opsForList().rightPush(key, userId);
    }

    // 큐에서 사용자 요청 하나 꺼내기 (선착순 입장)
    public String dequeueJoinRequest(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

}
