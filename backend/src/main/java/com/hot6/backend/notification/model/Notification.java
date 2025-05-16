package com.hot6.backend.notification.model;

import com.hot6.backend.common.BaseEntity;
import com.hot6.backend.schedule.model.Schedule;
import com.hot6.backend.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Entity
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private String message;
    private boolean isRead = false;
    private LocalDateTime sentAt;

    @OneToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;
}
