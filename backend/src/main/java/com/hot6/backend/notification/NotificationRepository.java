package com.hot6.backend.notification;

import com.hot6.backend.notification.model.Notification;
import com.hot6.backend.pet.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    int countByReceiver_IdxAndIsReadFalse(Long userId);
    List<Notification> findAllBySchedulePetOrderBySentAtDesc(Pet pet);
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.idx = :idx")
    void deleteById(Long idx);
}
