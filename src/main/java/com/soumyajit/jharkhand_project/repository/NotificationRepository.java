package com.soumyajit.jharkhand_project.repository;

import com.soumyajit.jharkhand_project.entity.Notification;
import com.soumyajit.jharkhand_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime after);

    long countByUser(User user);

}
