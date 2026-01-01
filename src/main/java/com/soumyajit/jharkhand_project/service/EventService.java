package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.CommentDto;
import com.soumyajit.jharkhand_project.dto.CreateEventRequest;
import com.soumyajit.jharkhand_project.dto.EventDto;
import com.soumyajit.jharkhand_project.entity.Comment;
import com.soumyajit.jharkhand_project.entity.Event;
import com.soumyajit.jharkhand_project.entity.PostStatus;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.repository.CommentRepository;
import com.soumyajit.jharkhand_project.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;


    @Cacheable(value = {"events","recent-events"}, key = "'approved'")
    public List<EventDto> getApprovedEvents() {
        List<Event> events = eventRepository.findByStatusOrderByCreatedAtDesc(PostStatus.APPROVED);
        return events.stream()
                .map(event -> modelMapper.map(event, EventDto.class))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"events", "recent-events"}, allEntries = true)
    public EventDto createEvent(CreateEventRequest request, List<MultipartFile> images, User author) {
        // ✅ Validate required fields
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Event title is required");
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Event description is required");
        }

        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("At least one event image is required");
        }

        PostStatus status = author.getRole().equals(User.Role.ADMIN)
                ? PostStatus.APPROVED
                : PostStatus.PENDING;

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .reglink(request.getReglink()) // ⚠️ Optional
                .eventDate(request.getEventDate()) // ⚠️ Optional
                .endDate(request.getEndDate()) // 🆕 Optional - End Date
                .location(request.getLocation()) // ⚠️ Optional
                .author(author)
                .status(status)
                .build();

        // ✅ FIXED - Upload images and get BOTH URLs and publicIds
        List<CloudinaryService.CloudinaryUploadResult> uploadResults =
                cloudinaryService.uploadImagesWithPublicIds(images);

        // Extract URLs
        List<String> imageUrls = uploadResults.stream()
                .map(CloudinaryService.CloudinaryUploadResult::getUrl)
                .collect(Collectors.toList());

        // Extract publicIds for future deletion
        List<String> publicIds = uploadResults.stream()
                .map(CloudinaryService.CloudinaryUploadResult::getPublicId)
                .collect(Collectors.toList());

        event.setImageUrls(imageUrls);
        event.setPublicIds(publicIds);  // ✅ Save publicIds!

        Event savedEvent = eventRepository.save(event);
        log.info("Created event with ID: {} by user: {}", savedEvent.getId(), author.getEmail());

        return modelMapper.map(savedEvent, EventDto.class);
    }



    @CacheEvict(value = {"events", "recent-events"}, allEntries = true)
    public EventDto approveEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        event.setStatus(PostStatus.APPROVED);
        Event savedEvent = eventRepository.save(event);
        log.info("Approved event with ID: {}", eventId);

        // ✅ UPDATED: Send notification to event owner with reference
        notificationService.notifyUser(
                event.getAuthor().getId(),
                "Your event '" + event.getTitle() + "' has been approved!",
                eventId,          // Reference ID
                "EVENT"           // Reference Type
        );

        return modelMapper.map(savedEvent, EventDto.class);
    }



    public List<EventDto> getPendingEvents() {
        List<Event> events = eventRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PENDING);
        return events.stream()
                .map(event -> modelMapper.map(event, EventDto.class))
                .collect(Collectors.toList());
    }

    public EventDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + id));

        EventDto dto = modelMapper.map(event, EventDto.class);

        List<Comment> comments = commentRepository.findByEventIdOrderByCreatedAtAsc(id);
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());

        dto.setComments(commentDtos);
        return dto;
    }

    @CacheEvict(value = {"events","recent-events"}, allEntries = true)
    public void deleteEvent(Long eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        boolean isAdmin = user.getRole().equals(User.Role.ADMIN);
        boolean isOwner = event.getAuthor().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Unauthorized: You can only delete your own events");
        }

        // ✅ FIXED - Use deleteImages() with List
        if (event.getPublicIds() != null && !event.getPublicIds().isEmpty()) {
            cloudinaryService.deleteImages(event.getPublicIds());
        }

        commentRepository.deleteByEventId(eventId);
        eventRepository.delete(event);

        log.info("Event deleted with ID: {} by user: {} ({})",
                eventId,
                user.getEmail(),
                isAdmin ? "ADMIN" : "OWNER"
        );
    }



    @Cacheable(value = "recent-events", key = "#days")
    public List<EventDto> getRecentEvents(int days) {
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(days);

        List<Event> recentEvents = eventRepository
                .findByStatusAndCreatedAtAfterOrderByCreatedAtDesc(PostStatus.APPROVED, dateThreshold);

        return recentEvents.stream()
                .map(event -> modelMapper.map(event, EventDto.class))
                .collect(Collectors.toList());
    }


}
