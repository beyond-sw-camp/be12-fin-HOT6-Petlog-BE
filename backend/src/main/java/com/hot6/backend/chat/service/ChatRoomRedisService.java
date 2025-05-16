package com.hot6.backend.chat.service;

import com.hot6.backend.chat.model.ChatRoom;
import com.hot6.backend.chat.repository.ChatRoomRepository;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import com.hot6.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatRoomRedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomJoinRedisRepository chatRoomJoinRedisRepository;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final RedissonClient redissonClient;

    public void requestJoin(User user, Long roomIdx) {
        String queueKey = "chatroom:join:queue:" + roomIdx;
        String userId = String.valueOf(user.getIdx());

        // 중복 요청 방지용 Redis Set 체크
        if (chatRoomJoinRedisRepository.sIsMember("chatroom.join.roomId." + roomIdx, userId)) {
            throw new BaseException(BaseResponseStatus.CHAT_ROOM_ALREADY_PARTICIPATED);
        }

        // 큐에 넣기 (락 없이)
        chatRoomJoinRedisRepository.enqueueJoinRequest(queueKey, userId);

        // Optional: 사용자 상태 저장 (대기 중 표시)
        redisTemplate.opsForValue().set("chatroom:join:status:" + roomIdx + ":" + userId, "WAITING", 5, TimeUnit.MINUTES);
    }


    public void join(User user, Long roomIdx) {
        String lockKey = "lock:chatroom:join:" + roomIdx;
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(3, 5, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new BaseException(BaseResponseStatus.LOCK_ACQUIRE_FAILED);
            }

            ChatRoom chatRoom = chatRoomRepository.findByIdx(roomIdx)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));

            String queueKey = "chatroom:join:queue:" + roomIdx;
            String participantsKey = "chatroom.join.roomId." + roomIdx;
            String userIdStr = String.valueOf(user.getIdx());

            // 이미 참가한 유저는 큐에 추가하지 않음
            if (chatRoomJoinRedisRepository.sIsMember(participantsKey, userIdStr)) {
                System.out.println("⚠️ 이미 입장한 유저 중복 요청 차단: " + userIdStr);
                throw new BaseException(BaseResponseStatus.CHAT_ROOM_ALREADY_PARTICIPATED);
            }

            // 큐에 참여 요청 추가
            chatRoomJoinRedisRepository.enqueueJoinRequest(queueKey, userIdStr);

            // 큐에서 현재 사용자 가져오기 (선착순)
            String currentTurnUserId = chatRoomJoinRedisRepository.dequeueJoinRequest(queueKey);

            // 선착순 사용자만 입장 허용
            if (!userIdStr.equals(currentTurnUserId)) {
                throw new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_YOUR_TURN);
            }

            // 제한 인원 초과 시 입장 불가
            if (chatRoom.getMaxParticipants() <= chatRoomJoinRedisRepository.sCard(participantsKey)) {
                throw new BaseException(BaseResponseStatus.CHAT_ROOM_FULL);
            }

            chatRoomJoinRedisRepository.sAdd(participantsKey, userIdStr);
            chatRoomParticipantService.join(user, chatRoom);

        } catch (InterruptedException e) {
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    public void processJoinQueue() {
        // Redis에서 모든 채팅방 큐 키 검색
        Set<String> keys = redisTemplate.keys("chatroom:join:queue:*");

        if (keys == null) return;

        for (String queueKey : keys) {
            String roomIdxStr = queueKey.replace("chatroom:join:queue:", "");
            Long roomIdx = Long.parseLong(roomIdxStr);

            for (int i = 0; i < 10; i++) { // 한 번에 최대 10명씩 처리
                String userId = chatRoomJoinRedisRepository.dequeueJoinRequest(queueKey);
                if (userId == null) break;

                try {
                    ChatRoom chatRoom = chatRoomRepository.findByIdx(roomIdx)
                            .orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));

                    String participantsKey = "chatroom.join.roomId." + roomIdx;

                    if (chatRoomJoinRedisRepository.sIsMember(participantsKey, userId)) {
                        redisTemplate.opsForValue().set("chatroom:join:status:" + roomIdx + ":" + userId, "ALREADY_JOINED", 1, TimeUnit.MINUTES);
                        continue;
                    }

                    if (chatRoomJoinRedisRepository.sCard(participantsKey) >= chatRoom.getMaxParticipants()) {
                        redisTemplate.opsForValue().set("chatroom:join:status:" + roomIdx + ":" + userId, "FULL", 3, TimeUnit.MINUTES);
                        continue;
                    }

                    chatRoomJoinRedisRepository.sAdd(participantsKey, userId);
                    chatRoomParticipantService.join(User.builder().idx(Long.valueOf(userId)).build(), chatRoom);
                    redisTemplate.opsForValue().set("chatroom:join:status:" + roomIdx + ":" + userId, "JOINED", 5, TimeUnit.MINUTES);

                } catch (Exception e) {
                    redisTemplate.opsForValue().set("chatroom:join:status:" + roomIdx + ":" + userId, "FAILED", 5, TimeUnit.MINUTES);
                }
            }
        }
    }

    public String getJoinStatus(Long roomIdx, Long userId) {
        String key = "chatroom:join:status:" + roomIdx + ":" + userId;
        return redisTemplate.opsForValue().get(key);
    }


}
