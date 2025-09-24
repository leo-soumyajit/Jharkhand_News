package com.soumyajit.jharkhand_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.CreateEventRequest;
import com.soumyajit.jharkhand_project.dto.EventDto;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventDto>>> getApprovedEvents() {
        try {
            List<EventDto> events = eventService.getApprovedEvents();
            return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
        } catch (Exception e) {
            log.error("Error retrieving approved events", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve events"));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EventDto>>> getPendingEvents() {
        try {
            List<EventDto> events = eventService.getPendingEvents();
            return ResponseEntity.ok(ApiResponse.success("Pending events retrieved successfully", events));
        } catch (Exception e) {
            log.error("Error retrieving pending events", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve pending events"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDto>> getEventById(@PathVariable Long id) {
        try {
            EventDto event = eventService.getEventById(id);
            return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", event));
        } catch (Exception e) {
            log.error("Error retrieving event with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventDto>> createEvent(
            @RequestPart("event") String eventJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();

            // Parse JSON string manually
            CreateEventRequest request = objectMapper.readValue(eventJson, CreateEventRequest.class);

            EventDto event = eventService.createEvent(request, images, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Event created successfully and pending approval", event));
        } catch (Exception e) {
            log.error("Error creating event", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create event"));
        }
    }

    @PostMapping("/{eventId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventDto>> approveEvent(@PathVariable Long eventId) {
        try {
            EventDto event = eventService.approveEvent(eventId);
            return ResponseEntity.ok(ApiResponse.success("Event approved successfully", event));
        } catch (Exception e) {
            log.error("Error approving event with ID: {}", eventId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to approve event"));
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteEvent(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            eventService.deleteEvent(id, user);
            return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting event with ID: {}", id, e);
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only delete your own events or be an admin"));
            }
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete event"));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<EventDto>>> getRecentEvents(
            @RequestParam(defaultValue = "15") int days) {
        try {
            List<EventDto> recentEvents = eventService.getRecentEvents(days);
            return ResponseEntity.ok(ApiResponse.success("Recent events retrieved successfully", recentEvents));
        } catch (Exception e) {
            log.error("Error retrieving recent events for last {} days", days, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent events"));
        }
    }


}
