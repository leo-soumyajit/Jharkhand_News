package com.soumyajit.jharkhand_project.service;


import com.soumyajit.jharkhand_project.dto.CommentDto;
import com.soumyajit.jharkhand_project.dto.CreateDistrictNewsRequest;
import com.soumyajit.jharkhand_project.dto.DistrictNewsDto;
import com.soumyajit.jharkhand_project.dto.UpdateDistrictNewsRequest;
import com.soumyajit.jharkhand_project.entity.Comment;
import com.soumyajit.jharkhand_project.entity.District;
import com.soumyajit.jharkhand_project.entity.DistrictNews;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.repository.CommentRepository;
import com.soumyajit.jharkhand_project.repository.DistrictNewsRepository;
import com.soumyajit.jharkhand_project.repository.DistrictRepository;
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
public class DistrictNewsService {

    private final DistrictNewsRepository districtNewsRepository;
    private final DistrictRepository districtRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;

    @Cacheable(value = "district-news", key = "#districtName")
    public List<DistrictNewsDto> getNewsByDistrict(String districtName) {
        District district = districtRepository.findByNameIgnoreCase(districtName)
                .orElseThrow(() -> new EntityNotFoundException("District not found: " + districtName));

        List<DistrictNews> news = districtNewsRepository
                .findByDistrictAndPublishedTrueOrderByCreatedAtDesc(district);

        return news.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "recent-district-news", key = "#districtName + '_' + #days")
    public List<DistrictNewsDto> getRecentNewsByDistrict(String districtName, int days) {
        District district = districtRepository.findByNameIgnoreCase(districtName)
                .orElseThrow(() -> new EntityNotFoundException("District not found: " + districtName));

        // Calculate date threshold (last N days)
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(days);

        List<DistrictNews> recentNews = districtNewsRepository
                .findByDistrictAndPublishedTrueAndCreatedAtAfterOrderByCreatedAtDesc(district, dateThreshold);

        return recentNews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }






    @CacheEvict(value = "district-news", allEntries = true)
    public DistrictNewsDto createDistrictNews(CreateDistrictNewsRequest request,
                                              List<MultipartFile> images,
                                              User author) {
        District district = districtRepository.findByNameIgnoreCase(request.getDistrictName())
                .orElseThrow(() -> new EntityNotFoundException("District not found: " + request.getDistrictName()));

        DistrictNews news = modelMapper.map(request, DistrictNews.class);
        news.setDistrict(district);
        news.setAuthor(author);
        news.setPublished(true);

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = cloudinaryService.uploadImages(images);
            news.setImageUrls(imageUrls);
        }

        DistrictNews savedNews = districtNewsRepository.save(news);
        log.info("Created district news with ID: {} for district: {}", savedNews.getId(), district.getName());

        return convertToDto(savedNews);
    }

    public DistrictNewsDto getNewsById(Long id) {
        DistrictNews news = districtNewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("News not found with ID: " + id));

        return convertToDtoWithComments(news);
    }

    private DistrictNewsDto convertToDto(DistrictNews news) {
        DistrictNewsDto dto = modelMapper.map(news, DistrictNewsDto.class);
        dto.setDistrictName(news.getDistrict().getName());
        return dto;
    }

    private DistrictNewsDto convertToDtoWithComments(DistrictNews news) {
        DistrictNewsDto dto = convertToDto(news);

        List<Comment> comments = commentRepository.findByDistrictNewsIdOrderByCreatedAtDesc(news.getId());
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());

        dto.setComments(commentDtos);
        return dto;
    }

    public DistrictNewsDto updateDistrictNews(Long id, UpdateDistrictNewsRequest request, User user) {
        DistrictNews existingNews = districtNewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("News not found with ID: " + id));

        // Security check - only author or admin can update
        if (!existingNews.getAuthor().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("Unauthorized: You can only update your own news articles");
        }

        // Find District entity by name
        District district = districtRepository.findByName(request.getDistrictName())
                .orElseThrow(() -> new RuntimeException("District not found: " + request.getDistrictName()));

        existingNews.setTitle(request.getTitle());
        existingNews.setContent(request.getContent());
        existingNews.setDistrict(district);

        existingNews.setUpdatedAt(LocalDateTime.now());

        try {
            DistrictNews savedNews = districtNewsRepository.save(existingNews);
            log.info("Successfully updated district news text content with ID: {} by user: {}", id, user.getEmail());

            return modelMapper.map(savedNews, DistrictNewsDto.class);
        } catch (Exception e) {
            log.error("Error saving updated district news with ID: {}", id, e);
            throw new RuntimeException("Failed to update news: " + e.getMessage());
        }
    }


    // Delete news
    public void deleteDistrictNews(Long id, User user) {
        DistrictNews news = districtNewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("News not found with ID: " + id));

        // Check if user owns this news (security check)
        if (!news.getAuthor().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("Unauthorized: You can only delete your own news");
        }

        districtNewsRepository.delete(news);
        log.info("News deleted with ID: {} by user: {}", id, user.getEmail());
    }



}

