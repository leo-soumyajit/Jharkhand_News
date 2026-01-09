package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.entity.LoginHistory;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    @Transactional
    public void saveLoginHistory(User user, String device, String ipAddress,
                                 String location, User.AuthProvider authProvider,
                                 Boolean success) {
        try {
            LoginHistory loginHistory = LoginHistory.builder()
                    .user(user)
                    .device(device)
                    .ipAddress(ipAddress)
                    .location(location)
                    .authProvider(authProvider)
                    .success(success)
                    .build();

            loginHistoryRepository.save(loginHistory);
            log.info("Login history saved for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error saving login history for user: {}", user.getEmail(), e);
        }
    }

    public Page<LoginHistory> getUserLoginHistory(Long userId, Pageable pageable) {
        return loginHistoryRepository.findByUserIdOrderByLoginTimeDesc(userId, pageable);
    }

    public List<LoginHistory> getRecentLoginHistory(Long userId) {
        return loginHistoryRepository.findTop10ByUserIdOrderByLoginTimeDesc(userId);
    }

    public Long getTotalLoginCount(Long userId) {
        return loginHistoryRepository.countByUserId(userId);
    }
}
