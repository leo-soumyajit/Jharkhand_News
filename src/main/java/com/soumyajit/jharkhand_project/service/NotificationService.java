package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.NotificationDto;
import com.soumyajit.jharkhand_project.entity.DistrictNews;
import com.soumyajit.jharkhand_project.entity.Notification;
import com.soumyajit.jharkhand_project.entity.Subscriber;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.exception.AccessDeniedException;
import com.soumyajit.jharkhand_project.exception.EntityNotFoundException;
import com.soumyajit.jharkhand_project.repository.NotificationRepository;
import com.soumyajit.jharkhand_project.repository.SubscriberRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SubscriberRepository subscriberRepository;
    private final JavaMailSender mailSender;

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

    @Transactional
    public void deleteNotificationByIdAndUser(Long id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with ID: " + id));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Unauthorized to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotificationsByUser(User user) {
        notificationRepository.deleteByUser(user);
    }

    public void sendFullNewsEmail(DistrictNews news) {
        List<Subscriber> subscribers = subscriberRepository.findBySubscribedTrue();
        String subject = "New post: " + news.getTitle();
        String body = buildStyledNewsEmailBody(news); // See body builder below

        for (Subscriber subscriber : subscribers) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom("thepeoplespress8@gmail.com");
                helper.setTo(subscriber.getEmail());
                helper.setSubject(subject);
                helper.setText(body, true); // 'true' means HTML

                mailSender.send(message);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + subscriber.getEmail() + ": " + e.getMessage());
            }
        }
    }

    private String buildStyledNewsEmailBody(DistrictNews news) {
        StringBuilder imagesHtml = new StringBuilder();
        if (news.getImageUrls() != null && !news.getImageUrls().isEmpty()) {
            imagesHtml.append("<div style='display:flex; overflow-x:auto; gap:10px; margin:20px 0;'>");
            for (String url : news.getImageUrls()) {
                imagesHtml.append("<img src='").append(url).append("' style='max-height:250px; border-radius:10px; margin-right:15px; box-shadow: 0 4px 8px rgba(0,0,0,0.15);' />");
            }
            imagesHtml.append("</div>");
        }

        String authorName = news.getAuthor().getFirstName() + " " + news.getAuthor().getLastName();

        String footerHtml = """
<div style="font-family: 'Arial', sans-serif; font-size: 14px; color: #444;">
  <div style="display:flex; justify-content:space-between; margin-bottom: 20px;">
    <div>
      <strong>Follow us:</strong>
      <p>Stay connected with Jharkhand Times for the 
        latest updates, breaking news, and
        community stories from across all 24 districts.
      </p>
      <div style="display:flex; gap: 8px;">
        <a href="#" style="display:inline-block; width: 32px; height: 32px; background:#000; color:#fff; border-radius:4px; text-align:center; text-decoration:none; line-height:32px;">❌</a>
        <a href="#" style="display:inline-block; width: 32px; height: 32px; background:#3b5998; color:#fff; border-radius:4px; text-align:center; text-decoration:none; line-height:32px;">f</a>
        <a href="#" style="display:inline-block; width: 32px; height: 32px; background:#d6249f; color:#fff; border-radius:4px; text-align:center; text-decoration:none; line-height:32px;">📸</a>
        <a href="#" style="display:inline-block; width: 32px; height: 32px; background:#0077b5; color:#fff; border-radius:4px; text-align:center; text-decoration:none; line-height:32px;">in</a>
      </div>
    </div>
    <div>
      <strong>Contact us:</strong>
      <p>📧 info@jharkhandtimes.com</p>
      <p>📞 +91 (651) 234-5678</p>
      <p>📍 Press Club Building, Main Road<br /> Ranchi - 834001, Jharkhand</p>
    </div>
  </div>
  <hr style="border:none; border-top:1px solid #ccc; margin-bottom:12px;" />
  <p style="font-style: italic; color:#888;">Jharkhand Times<br />Connecting Jharkhand • Empowering Communities • Building Tomorrow</p>
  <p style="font-size: 12px; color:#666;">© 2025 Jharkhand Times. All rights reserved.<br />Privacy Policy | Terms of Service | Unsubscribe</p>
</div>
""";

        return "<html><body style='font-family: Georgia, serif; color:#222; margin:20px; line-height:1.6;'>" +
                "<h1 style='color:#003366; font-weight:bold; margin-bottom:0.2em;'>" + news.getTitle() + "</h1>" +
                "<p style='font-style: italic; color:#666; margin-top:0; font-size:14px;'>District: " + news.getDistrict().getName() +
                " | Author: " + authorName + "</p>" +
                "<hr style='border:none; border-top:2px solid #004080; margin:15px 0;' />" +
                imagesHtml.toString() +
                "<div style='font-size:16px; margin-top:20px; text-align:justify;'>" +
                news.getContent() +
                "</div>" +
                "<hr style='border:none; border-top:1px solid #ccc; margin-top:40px; margin-bottom:20px;' />" +
                footerHtml +
                "</body></html>";
    }







}

