package com.soumyajit.jharkhand_project.service;


import com.soumyajit.jharkhand_project.dto.CommentDto;
import com.soumyajit.jharkhand_project.dto.CommunityPostDto;
import com.soumyajit.jharkhand_project.dto.CreateCommunityPostRequest;
import com.soumyajit.jharkhand_project.entity.Comment;
import com.soumyajit.jharkhand_project.entity.CommunityPost;
import com.soumyajit.jharkhand_project.entity.PostStatus;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.repository.CommentRepository;
import com.soumyajit.jharkhand_project.repository.CommunityPostRepository;
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
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Cacheable(value = "community-posts", key = "'approved'")
    public List<CommunityPostDto> getApprovedPosts() {
        List<CommunityPost> posts = communityPostRepository.findByStatusOrderByCreatedAtDesc(PostStatus.APPROVED);
        return posts.stream()
                .map(post -> modelMapper.map(post, CommunityPostDto.class))
                .collect(Collectors.toList());
    }

    public CommunityPostDto createPost(CreateCommunityPostRequest request, List<MultipartFile> images, User author) {
        CommunityPost post = modelMapper.map(request, CommunityPost.class);
        post.setAuthor(author);
        post.setStatus(PostStatus.PENDING);

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = cloudinaryService.uploadImages(images);
            post.setImageUrls(imageUrls);
        }

        CommunityPost savedPost = communityPostRepository.save(post);
        log.info("Created community post with ID: {} by user: {}", savedPost.getId(), author.getEmail());

        return modelMapper.map(savedPost, CommunityPostDto.class);
    }

    @CacheEvict(value = {"community-posts", "recent-community-posts"}, allEntries = true)
    public CommunityPostDto approvePost(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Community post not found with ID: " + postId));

        post.setStatus(PostStatus.APPROVED);
        CommunityPost savedPost = communityPostRepository.save(post);
        log.info("Approved community post with ID: {}", postId);

        // Notify the author about approval
        notificationService.notifyUser(post.getAuthor().getId(),
                "Your community post '" + post.getTitle() + "' has been approved!");

        return modelMapper.map(savedPost, CommunityPostDto.class);
    }


    public List<CommunityPostDto> getPendingPosts() {
        List<CommunityPost> posts = communityPostRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PENDING);
        return posts.stream()
                .map(post -> modelMapper.map(post, CommunityPostDto.class))
                .collect(Collectors.toList());
    }

    public CommunityPostDto getPostById(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Community post not found with ID: " + id));

        CommunityPostDto dto = modelMapper.map(post, CommunityPostDto.class);

        List<Comment> comments = commentRepository.findByCommunityPostIdOrderByCreatedAtDesc(id);
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());

        dto.setComments(commentDtos);
        return dto;
    }

    @CacheEvict(value = {"community-posts","recent-community-posts"}, allEntries = true)
    public void deleteCommunityPost(Long postId, User user) {
        // Find the community post
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Community post not found with ID: " + postId));


        boolean isAdmin = user.getRole().equals(User.Role.ADMIN);
        boolean isOwner = post.getAuthor().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Unauthorized: You can only delete your own community posts");
        }

        commentRepository.deleteByCommunityPostId(postId);

        communityPostRepository.delete(post);

        log.info("Community post deleted with ID: {} by user: {} ({})",
                postId,
                user.getEmail(),
                isAdmin ? "ADMIN" : "OWNER"
        );
    }

    @Cacheable(value = "recent-community-posts", key = "#days")
    public List<CommunityPostDto> getRecentPosts(int days) {
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(days);

        List<CommunityPost> recentPosts = communityPostRepository
                .findByStatusAndCreatedAtAfterOrderByCreatedAtDesc(PostStatus.APPROVED, dateThreshold);

        return recentPosts.stream()
                .map(post -> modelMapper.map(post, CommunityPostDto.class))
                .collect(Collectors.toList());
    }


}
