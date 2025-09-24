package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.NotificationDto;
import com.soumyajit.jharkhand_project.entity.Notification;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Create and save a notification for a user
    @Async
    public void notifyUser(Long userId, String message) {
        Notification notification = new Notification();
        User user = new User();
        user.setId(userId);
        notification.setUser(user);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // Get recent notifications for a user in the past 'days'
    public List<NotificationDto> getRecentNotifications(Long userId, int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        List<Notification> notifications =
                notificationRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, threshold);
        return notifications.stream()
                .map(n -> new NotificationDto(n.getId(), n.getMessage(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }
}

