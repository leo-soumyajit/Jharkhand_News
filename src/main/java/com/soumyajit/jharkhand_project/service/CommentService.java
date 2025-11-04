package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.CommentDto;
import com.soumyajit.jharkhand_project.dto.CreateCommentRequest;
import com.soumyajit.jharkhand_project.entity.*;
import com.soumyajit.jharkhand_project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final DistrictNewsRepository districtNewsRepository;
    private final EventRepository eventRepository;
    private final JobRepository jobRepository;
    private final CommunityPostRepository communityPostRepository;
    private final NotificationService notificationService; // injected notification service
    private final ModelMapper modelMapper;

    public CommentDto createDistrictNewsComment(Long newsId, CreateCommentRequest request, User author) {
        DistrictNews districtNews = districtNewsRepository.findById(newsId)
                .orElseThrow(() -> new EntityNotFoundException("District news not found with ID: " + newsId));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setDistrictNewsId(districtNews.getId());

        Comment savedComment = commentRepository.save(comment);
        log.info("Created district news comment with ID: {} by user: {}", savedComment.getId(), author.getEmail());

        // Notify the district news author if not the same user
        if (!districtNews.getAuthor().getId().equals(author.getId())) {
            notificationService.notifyUser(districtNews.getAuthor().getId(),
                    author.getFirstName() +" "+author.getLastName() + " commented on your news article '" + districtNews.getTitle() + "'");
        }

        return modelMapper.map(savedComment, CommentDto.class);
    }

    public CommentDto createEventComment(Long eventId, CreateCommentRequest request, User author) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setEventId(event.getId());

        Comment savedComment = commentRepository.save(comment);
        log.info("Created event comment with ID: {} by user: {}", savedComment.getId(), author.getEmail());

        // Notify the event author if not the same user
        if (!event.getAuthor().getId().equals(author.getId())) {
            notificationService.notifyUser(event.getAuthor().getId(),
                    author.getFirstName() +" "+author.getLastName() + " commented on your event '" + event.getTitle() + "'");
        }

        return modelMapper.map(savedComment, CommentDto.class);
    }

    public CommentDto createJobComment(Long jobId, CreateCommentRequest request, User author) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found with ID: " + jobId));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setJobId(job.getId());

        Comment savedComment = commentRepository.save(comment);
        log.info("Created job comment with ID: {} by user: {}", savedComment.getId(), author.getEmail());

        // Notify the job author if not the same user
        if (!job.getAuthor().getId().equals(author.getId())) {
            notificationService.notifyUser(job.getAuthor().getId(),
                    author.getFirstName() +" "+author.getLastName() + " commented on your job posting '" + job.getTitle() + "'");
        }

        return modelMapper.map(savedComment, CommentDto.class);
    }

    public CommentDto createCommunityPostComment(Long postId, CreateCommentRequest request, User author) {
        CommunityPost communityPost = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Community post not found with ID: " + postId));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setCommunityPostId(communityPost.getId());

        Comment savedComment = commentRepository.save(comment);
        log.info("Created community post comment with ID: {} by user: {}", savedComment.getId(), author.getEmail());

        // Notify community post author if not the same user
        if (!communityPost.getAuthor().getId().equals(author.getId())) {
            notificationService.notifyUser(communityPost.getAuthor().getId(),
                    author.getFirstName() +" "+author.getLastName() + " commented on your community post '" + communityPost.getTitle() + "'");
        }

        return modelMapper.map(savedComment, CommentDto.class);
    }

    public List<CommentDto> getDistrictNewsComments(Long newsId) {
        List<Comment> comments = commentRepository.findByDistrictNewsIdOrderByCreatedAtDesc(newsId);
        return comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());
    }

    public List<CommentDto> getEventComments(Long eventId) {
        List<Comment> comments = commentRepository.findByEventIdOrderByCreatedAtDesc(eventId);
        return comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());
    }

    public List<CommentDto> getJobComments(Long jobId) {
        List<Comment> comments = commentRepository.findByJobIdOrderByCreatedAtDesc(jobId);
        return comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommunityPostComments(Long postId) {
        List<Comment> comments = commentRepository.findByCommunityPostIdOrderByCreatedAtDesc(postId);
        return comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentsForPost(String postType, Long postId) {
        switch (postType.toLowerCase()) {
            case "district-news":
                return getDistrictNewsComments(postId);
            case "event":
                return getEventComments(postId);
            case "job":
                return getJobComments(postId);
            case "community":
                return getCommunityPostComments(postId);
            default:
                throw new IllegalArgumentException("Invalid post type: " + postType);
        }
    }



    public CommentDto updateComment(Long commentId, CreateCommentRequest request, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

        // Check if the current user is the comment author
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new SecurityException("You are not authorized to update this comment");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        log.info("Updated comment with ID: {} by user: {}", commentId, currentUser.getEmail());

        return modelMapper.map(updatedComment, CommentDto.class);
    }

    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

        boolean isCommentOwner = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isPostOwner = false;

        // Check if current user is the post owner based on post type
        if (comment.getDistrictNewsId() != null) {
            DistrictNews news = districtNewsRepository.findById(comment.getDistrictNewsId())
                    .orElseThrow(() -> new EntityNotFoundException("District news not found"));
            isPostOwner = news.getAuthor().getId().equals(currentUser.getId());
        } else if (comment.getEventId() != null) {
            Event event = eventRepository.findById(comment.getEventId())
                    .orElseThrow(() -> new EntityNotFoundException("Event not found"));
            isPostOwner = event.getAuthor().getId().equals(currentUser.getId());
        } else if (comment.getJobId() != null) {
            Job job = jobRepository.findById(comment.getJobId())
                    .orElseThrow(() -> new EntityNotFoundException("Job not found"));
            isPostOwner = job.getAuthor().getId().equals(currentUser.getId());
        } else if (comment.getCommunityPostId() != null) {
            CommunityPost post = communityPostRepository.findById(comment.getCommunityPostId())
                    .orElseThrow(() -> new EntityNotFoundException("Community post not found"));
            isPostOwner = post.getAuthor().getId().equals(currentUser.getId());
        }

        // Allow deletion if user is either comment owner or post owner
        if (!isCommentOwner && !isPostOwner) {
            throw new SecurityException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
        log.info("Deleted comment with ID: {} by user: {}", commentId, currentUser.getEmail());
    }

}
