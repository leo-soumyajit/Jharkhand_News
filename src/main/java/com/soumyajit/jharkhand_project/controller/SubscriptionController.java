package com.soumyajit.jharkhand_project.controller;

import com.soumyajit.jharkhand_project.service.SubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscribe")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriberService subscriberService;

    @PostMapping
    public ResponseEntity<?> subscribe(@RequestParam String email) {
        subscriberService.subscribe(email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> unsubscribe(@RequestParam String email) {
        subscriberService.unsubscribe(email);
        return ResponseEntity.ok().build();
    }
}
