package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.StateNewsDto;
import com.soumyajit.jharkhand_project.dto.ViewHistoryDto;
import com.soumyajit.jharkhand_project.entity.StateNews;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.entity.ViewHistory;
import com.soumyajit.jharkhand_project.repository.StateNewsRepository;
import com.soumyajit.jharkhand_project.repository.ViewHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewHistoryService {

    private final ViewHistoryRepository viewHistoryRepository;
    private final StateNewsRepository stateNewsRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public void trackView(User user, ViewHistory.ContentType contentType, Long contentId) {
        if (user == null) {
            log.debug("Anonymous user - not tracking view history");
            return;
        }

        try {
            Optional<ViewHistory> existing = viewHistoryRepository
                    .findByUserIdAndContentTypeAndContentId(user.getId(), contentType, contentId);

            if (existing.isPresent()) {
                ViewHistory history = existing.get();
                history.setViewedAt(LocalDateTime.now());
                viewHistoryRepository.save(history);
                log.debug("Updated view history for user {} - {} ID: {}",
                        user.getId(), contentType, contentId);
            } else {
                ViewHistory newHistory = ViewHistory.builder()
                        .user(user)
                        .contentType(contentType)
                        .contentId(contentId)
                        .build();
                viewHistoryRepository.save(newHistory);
                log.debug("Created view history for user {} - {} ID: {}",
                        user.getId(), contentType, contentId);
            }
        } catch (Exception e) {
            log.error("Error tracking view history for user {} - {} ID: {}",
                    user.getId(), contentType, contentId, e);
        }
    }

    public Page<ViewHistoryDto> getHistoryWithDetails(User user, ViewHistory.ContentType contentType, Pageable pageable) {
        Page<ViewHistory> historyPage = viewHistoryRepository
                .findByUserIdAndContentTypeOrderByViewedAtDesc(user.getId(), contentType, pageable);

        return historyPage.map(history -> {
            ViewHistoryDto dto = ViewHistoryDto.builder()
                    .id(history.getId())
                    .contentType(history.getContentType())
                    .contentId(history.getContentId())
                    .viewedAt(history.getViewedAt())
                    .build();

            if (contentType == ViewHistory.ContentType.STATE_NEWS) {
                stateNewsRepository.findById(history.getContentId()).ifPresent(news -> {
                    StateNewsDto newsDto = modelMapper.map(news, StateNewsDto.class);
                    newsDto.setStateName(news.getState().getName());
                    dto.setContentDetails(newsDto);
                });
            }

            return dto;
        });
    }

    @Transactional
    public void deleteSingleHistory(User user, ViewHistory.ContentType contentType, Long contentId) {
        viewHistoryRepository.deleteByUserIdAndContentTypeAndContentId(
                user.getId(), contentType, contentId);
        log.info("Deleted single history for user {} - {} ID: {}",
                user.getId(), contentType, contentId);
    }

    @Transactional
    public void clearAllHistory(User user, ViewHistory.ContentType contentType) {
        viewHistoryRepository.deleteByUserIdAndContentType(user.getId(), contentType);
        log.info("Cleared all {} history for user {}", contentType, user.getId());
    }

    public Long getHistoryCount(User user, ViewHistory.ContentType contentType) {
        return viewHistoryRepository.countByUserIdAndContentType(user.getId(), contentType);
    }
}
