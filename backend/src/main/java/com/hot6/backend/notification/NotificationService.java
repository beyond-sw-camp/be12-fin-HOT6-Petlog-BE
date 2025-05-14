package com.hot6.backend.notification;

import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import com.hot6.backend.notification.model.Notification;
import com.hot6.backend.notification.model.NotificationDto;
import com.hot6.backend.pet.PetRepository;
import com.hot6.backend.pet.model.Pet;
import com.hot6.backend.schedule.ScheduleRepository;
import com.hot6.backend.schedule.model.Schedule;
import com.hot6.backend.user.UserRepository;
import com.hot6.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ScheduleRepository scheduleRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PetRepository petRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = false)
    public void createNotification(NotificationDto.NotificationSendRequest request, Long userIdx) {
        // 1. 스케줄 조회
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOTIFICATION_NOT_FOUND));

        // 2. 사용자 조회 (receiver 설정 위해)
        User receiver = userRepository.findById(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 3. 알림 생성 + 수신자 지정 (방법 1)
        Notification notification = NotificationDto.NotificationElement.builder()
                .message(request.getMessage())
                .sentAt(LocalDateTime.now())
                .build()
                .toEntity(schedule, receiver); // ✅ 한 줄로 수신자 포함 생성

        // 4. 저장
        notificationRepository.save(notification);

        // 5. 웹소켓 전송
        NotificationDto.NotificationElement element = NotificationDto.NotificationElement.builder()
                .idx(notification.getIdx())
                .message(notification.getMessage())
                .sentAt(notification.getSentAt())
                .petName(schedule.getPet().getName())
                .scheduleId(schedule.getIdx())
                .isRead(notification.isRead())
                .build();

        messagingTemplate.convertAndSend("/topic/alerts/" + userIdx, element);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto.NotificationElement> getNotificationsByUserId(Long userIdx) {
        List<NotificationDto.NotificationElement> list = new ArrayList<>();

        List<Pet> petList = petRepository.findByUserIdx(userIdx);

        for (Pet pet : petList) {

            List<Notification> notiList =  notificationRepository.findAllBySchedulePetOrderBySentAtDesc(pet);
            list.addAll(notiList.stream()
                    .map(NotificationDto.NotificationElement::from)
                    .toList());
        }

        return list;
    }

    @Transactional(readOnly = false)
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOTIFICATION_NOT_FOUND));

        notification.setRead(true); // ✅ 읽음 처리
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = false)
    public void deleteById(Long idx) {
        if (!notificationRepository.existsById(idx)) {
            throw new BaseException(BaseResponseStatus.NOTIFICATION_NOT_FOUND);
        }
        notificationRepository.deleteById(idx);
    }
    public int countUnreadByReceiverId(Long userId) {
        return notificationRepository.countByReceiver_IdxAndIsReadFalse(userId);
    }
}
