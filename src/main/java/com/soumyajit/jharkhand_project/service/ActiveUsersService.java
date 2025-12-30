package com.soumyajit.jharkhand_project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ActiveUsersService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Set<String> activeSessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void addUser(String sessionId) {
        activeSessions.add(sessionId);
        broadcastActiveUserCount();
    }

    public void removeUser(String sessionId) {
        activeSessions.remove(sessionId);
        broadcastActiveUserCount();
    }

    public int getActiveUserCount() {
        return activeSessions.size();
    }

    private void broadcastActiveUserCount() {
        int count = getActiveUserCount();
        messagingTemplate.convertAndSend("/topic/activeUsers", count);
        System.out.println("📊 Active users: " + count);
    }
}
