package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.CommentDto;
import com.soumyajit.jharkhand_project.dto.CreateStateNewsRequest;
import com.soumyajit.jharkhand_project.dto.StateNewsDto;
import com.soumyajit.jharkhand_project.dto.UpdateStateNewsRequest;
import com.soumyajit.jharkhand_project.entity.*;
import com.soumyajit.jharkhand_project.repository.CommentRepository;
import com.soumyajit.jharkhand_project.repository.StateNewsRepository;
import com.soumyajit.jharkhand_project.repository.StateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.soumyajit.jharkhand_project.entity.NewsCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StateNewsService {

    private final StateNewsRepository stateNewsRepository;
    private final StateRepository stateRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

//    @Cacheable(value = "state-news", key = "#stateName + '_' + #page + '_' + #size") commented as pagination issues
    public Page<StateNewsDto> getNewsByState(String stateName, int page, int size) {

        State state = stateRepository.findByNameIgnoreCase(stateName)
                .orElseThrow(() -> new EntityNotFoundException("State not found: " + stateName));

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");

        Page<StateNews> newsPage = stateNewsRepository
                .findByStateAndPublishedTrueOrderByCreatedAtDesc(state, pageable);

        return newsPage.map(this::convertToDto);
    }


    @Cacheable(value = "recent-state-news", key = "#stateName + '_' + #days")
    public List<StateNewsDto> getRecentNewsByState(String stateName, int days) {  // ✅ FIXED method name
        State state = stateRepository.findByNameIgnoreCase(stateName)
                .orElseThrow(() -> new EntityNotFoundException("State not found: " + stateName));

        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(days);

        List<StateNews> recentNews = stateNewsRepository
                .findByStateAndPublishedTrueAndCreatedAtAfterOrderByCreatedAtDesc(state, dateThreshold);  // ✅ FIXED

        return recentNews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"state-news", "recent-state-news"}, allEntries = true, beforeInvocation = true)  // ✅ FIXED
    public StateNewsDto createStateNews(CreateStateNewsRequest request,  // ✅ FIXED method name
                                        List<MultipartFile> images,
                                        User author) {
        State state = stateRepository.findByNameIgnoreCase(request.getStateName())
                .orElseThrow(() -> new EntityNotFoundException("State not found: " + request.getStateName()));

        StateNews news = modelMapper.map(request, StateNews.class);
        news.setState(state);
        news.setAuthor(author);
        news.setPublished(true);

        // ✅ NEW: Set category if provided (optional)
        if (request.getCategory() != null) {
            news.setCategory(request.getCategory());
        }

        if (images != null && !images.isEmpty()) {
            List<CloudinaryService.CloudinaryUploadResult> uploadResults =
                    cloudinaryService.uploadImagesWithPublicIds(images);

            List<String> imageUrls = uploadResults.stream()
                    .map(CloudinaryService.CloudinaryUploadResult::getUrl)
                    .collect(Collectors.toList());

            List<String> publicIds = uploadResults.stream()
                    .map(CloudinaryService.CloudinaryUploadResult::getPublicId)
                    .collect(Collectors.toList());

            news.setImageUrls(imageUrls);
            news.setCloudinaryPublicIds(publicIds);

            log.info("Uploaded {} images for state news", imageUrls.size());
        }

        StateNews savedNews = stateNewsRepository.save(news);
        log.info("Created state news with ID: {} for state: {}", savedNews.getId(), state.getName());

        notificationService.sendFullNewsEmail(savedNews);

        return convertToDto(savedNews);
    }

    public StateNewsDto getNewsById(Long id) {
        StateNews news = stateNewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("News not found with ID: " + id));

        return convertToDtoWithComments(news);
    }

    private StateNewsDto convertToDto(StateNews news) {
        StateNewsDto dto = modelMapper.map(news, StateNewsDto.class);
        dto.setStateName(news.getState().getName());
        return dto;
    }

    private StateNewsDto convertToDtoWithComments(StateNews news) {
        StateNewsDto dto = convertToDto(news);

        List<Comment> comments = commentRepository.findByStateNewsIdOrderByCreatedAtAsc(news.getId());  // ✅ FIXED
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());

        dto.setComments(commentDtos);
        return dto;
    }

    @CacheEvict(value = {"state-news", "recent-state-news"}, allEntries = true, beforeInvocation = true)
    public StateNewsDto updateStateNews(Long id, UpdateStateNewsRequest request, User user) {  // ✅ FIXED method name
        StateNews existingNews = stateNewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("News not found with ID: " + id));

        if (!existingNews.getAuthor().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("Unauthorized: You can only update your own news articles");
        }

        existingNews.setTitle(request.getTitle());
        existingNews.setContent(request.getContent());

        // ✅ NEW: Update category if provided
        if (request.getCategory() != null) {
            existingNews.setCategory(request.getCategory());
        }


        existingNews.setUpdatedAt(LocalDateTime.now());



        try {
            StateNews savedNews = stateNewsRepository.save(existingNews);
            log.info("Successfully updated state news text content with ID: {} by user: {}", id, user.getEmail());

            return modelMapper.map(savedNews, StateNewsDto.class);
        } catch (Exception e) {
            log.error("Error saving updated state news with ID: {}", id, e);
            throw new RuntimeException("Failed to update news: " + e.getMessage());
        }
    }

    @CacheEvict(value = {"state-news", "recent-state-news"}, allEntries = true, beforeInvocation = true)
    public void deleteStateNews(Long id, User user) {  // ✅ FIXED method name
        StateNews news = stateNewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("News not found with ID: " + id));

        if (!news.getAuthor().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("Unauthorized: You can only delete your own news");
        }

        if (news.getCloudinaryPublicIds() != null && !news.getCloudinaryPublicIds().isEmpty()) {
            log.info("Deleting {} images from Cloudinary for news ID: {}",
                    news.getCloudinaryPublicIds().size(), id);
            cloudinaryService.deleteImages(news.getCloudinaryPublicIds());
        } else {
            log.warn("No Cloudinary public_ids found for news ID: {}", id);
        }

        stateNewsRepository.delete(news);
        log.info("News deleted with ID: {} by user: {}", id, user.getEmail());
    }




    @Cacheable(value = "state-news", key = "#stateName + '_' + #category")
    public List<StateNewsDto> getNewsByStateAndCategory(String stateName, NewsCategory category) {
        State state = stateRepository.findByNameIgnoreCase(stateName)
                .orElseThrow(() -> new EntityNotFoundException("State not found: " + stateName));

        List<StateNews> news = stateNewsRepository
                .findByStateAndCategoryAndPublishedTrueOrderByCreatedAtDesc(state, category);

        return news.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "recent-state-news", key = "#stateName + '_' + #category + '_' + #days")
    public List<StateNewsDto> getRecentNewsByStateAndCategory(String stateName,
                                                              NewsCategory category,
                                                              int days) {
        State state = stateRepository.findByNameIgnoreCase(stateName)
                .orElseThrow(() -> new EntityNotFoundException("State not found: " + stateName));

        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(days);

        List<StateNews> news = stateNewsRepository
                .findByStateAndCategoryAndPublishedTrueAndCreatedAtAfterOrderByCreatedAtDesc(
                        state, category, dateThreshold
                );

        return news.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

}
