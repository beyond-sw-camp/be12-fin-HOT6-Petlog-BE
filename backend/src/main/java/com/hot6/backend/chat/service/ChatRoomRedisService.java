package com.hot6.backend.chat.service;

import com.hot6.backend.chat.model.ChatRoom;
import com.hot6.backend.chat.repository.ChatRoomRepository;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import com.hot6.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatRoomRedisService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomJoinRedisRepository chatRoomJoinRedisRepository;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final RedissonClient redissonClient;

    public void join(User user, Long roomIdx) {
        String lockKey = "lock:chatroom:join:" + roomIdx;
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLocked = false;

        try {
            // 락 획득 시도: 최대 3초 대기, 락 보유 시간은 5초
            isLocked = lock.tryLock(3, 5, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new BaseException(BaseResponseStatus.LOCK_ACQUIRE_FAILED); // 커스텀 예외
            }

            // 본 로직 시작
            ChatRoom chatRoom = chatRoomRepository.findByIdx(roomIdx)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.CHAT_ROOM_NOT_FOUND));

            String key = "chatroom.join.roomId." + roomIdx;

            if (chatRoomJoinRedisRepository.sIsMember(key, String.valueOf(user.getIdx()))) {
                throw new BaseException(BaseResponseStatus.CHAT_ROOM_ALREADY_PARTICIPATED);
            }

            if (chatRoom.getMaxParticipants() <= chatRoomJoinRedisRepository.sCard(key)) {
                throw new BaseException(BaseResponseStatus.CHAT_ROOM_FIRST_JOIN_ID_NULL);
            }

            chatRoomJoinRedisRepository.sAdd(key, String.valueOf(user.getIdx()));
            chatRoomParticipantService.join(user, chatRoom);

        } catch (InterruptedException e) {
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
