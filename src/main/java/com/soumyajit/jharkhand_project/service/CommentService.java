// In: src/main/java/com/soumyajit/jharkhand_project/service/CommentService.java

package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.AuthorDto;
import com.soumyajit.jharkhand_project.dto.CommentDto;
import com.soumyajit.jharkhand_project.dto.CreateCommentRequest;
import com.soumyajit.jharkhand_project.entity.*;
import com.soumyajit.jharkhand_project.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final StateNewsRepository stateNewsRepository;
    private final EventRepository eventRepository;
    private final JobRepository jobRepository;
    private final CommunityPostRepository communityPostRepository;
    private final PropertyRepository propertyRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    private interface PostInfoProvider {
        void setPostIdOnComment(Comment comment, Comment parent);
        User getPostAuthor();
        String getPostTitle();
        Long getPostId(); // ✅ NEW
        String getReferenceType(); // ✅ NEW
    }

    private CommentDto mapCommentToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        // Manually create and set AuthorDto
        if (comment.getAuthor() != null) {
            User authorEntity = comment.getAuthor();
            AuthorDto authorDto = new AuthorDto();
            authorDto.setId(authorEntity.getId());
            authorDto.setFirstName(authorEntity.getFirstName());
            authorDto.setLastName(authorEntity.getLastName());
            authorDto.setProfileImageUrl(authorEntity.getProfileImageUrl());
            authorDto.setRole(User.Role.valueOf(authorEntity.getRole().name()));
            dto.setAuthor(authorDto);
        }

        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }

        dto.setReplies(new ArrayList<>());

        return dto;
    }


    private List<CommentDto> buildCommentTree(List<Comment> comments) {
        Map<Long, CommentDto> commentDtoMap = new LinkedHashMap<>();

        for (Comment comment : comments) {
            commentDtoMap.put(comment.getId(), mapCommentToDto(comment));
        }

        List<CommentDto> rootComments = new ArrayList<>();
        for (CommentDto dto : commentDtoMap.values()) {
            if (dto.getParentId() != null) {
                CommentDto parentDto = commentDtoMap.get(dto.getParentId());
                if (parentDto != null) {
                    parentDto.getReplies().add(dto);
                }
            } else {
                rootComments.add(dto);
            }
        }

        return rootComments;
    }

    private CommentDto createCommentInternal(CreateCommentRequest request, User author, PostInfoProvider infoProvider) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);

        if (request.getParentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
            comment.setParentComment(parentComment);
            infoProvider.setPostIdOnComment(comment, parentComment);

            // ✅ UPDATED: Notify parent comment author with reference
            if (!parentComment.getAuthor().getId().equals(author.getId())) {
                notificationService.notifyUser(
                        parentComment.getAuthor().getId(),
                        author.getFirstName() + " " + author.getLastName() + " replied to your comment.",
                        infoProvider.getPostId(),
                        infoProvider.getReferenceType()
                );
            }
        } else {
            infoProvider.setPostIdOnComment(comment, null);
        }

        Comment savedComment = commentRepository.save(comment);
        User postAuthor = infoProvider.getPostAuthor();

        // ✅ UPDATED: Notify post author with reference
        if (!postAuthor.getId().equals(author.getId())) {
            notificationService.notifyUser(
                    postAuthor.getId(),
                    author.getFirstName() + " " + author.getLastName() + " commented on your post '" + infoProvider.getPostTitle() + "'",
                    infoProvider.getPostId(),
                    infoProvider.getReferenceType()
            );
        }

        return mapCommentToDto(savedComment);
    }

    public CommentDto createStateNewsComment(Long newsId, CreateCommentRequest request, User author) {
        StateNews news = stateNewsRepository.findById(newsId).orElseThrow(() -> new EntityNotFoundException("State news not found"));
        return createCommentInternal(request, author, new PostInfoProvider() {
            @Override public void setPostIdOnComment(Comment c, Comment p) { c.setStateNewsId(p != null ? p.getStateNewsId() : newsId); }
            @Override public User getPostAuthor() { return news.getAuthor(); }
            @Override public String getPostTitle() { return news.getTitle(); }
            @Override public Long getPostId() { return newsId; } // ✅ NEW
            @Override public String getReferenceType() { return "LOCAL_NEWS"; } // ✅ NEW
        });
    }

    public CommentDto createEventComment(Long eventId, CreateCommentRequest request, User author) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException("Event not found"));
        return createCommentInternal(request, author, new PostInfoProvider() {
            @Override public void setPostIdOnComment(Comment c, Comment p) { c.setEventId(p != null ? p.getEventId() : eventId); }
            @Override public User getPostAuthor() { return event.getAuthor(); }
            @Override public String getPostTitle() { return event.getTitle(); }
            @Override public Long getPostId() { return eventId; } // ✅ NEW
            @Override public String getReferenceType() { return "EVENT"; } // ✅ NEW
        });
    }

    public CommentDto createJobComment(Long jobId, CreateCommentRequest request, User author) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new EntityNotFoundException("Job not found"));
        return createCommentInternal(request, author, new PostInfoProvider() {
            @Override public void setPostIdOnComment(Comment c, Comment p) { c.setJobId(p != null ? p.getJobId() : jobId); }
            @Override public User getPostAuthor() { return job.getAuthor(); }
            @Override public String getPostTitle() { return job.getTitle(); }
            @Override public Long getPostId() { return jobId; } // ✅ NEW
            @Override public String getReferenceType() { return "JOB"; } // ✅ NEW
        });
    }

    public CommentDto createCommunityPostComment(Long postId, CreateCommentRequest request, User author) {
        CommunityPost post = communityPostRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Community post not found"));
        return createCommentInternal(request, author, new PostInfoProvider() {
            @Override public void setPostIdOnComment(Comment c, Comment p) { c.setCommunityPostId(p != null ? p.getCommunityPostId() : postId); }
            @Override public User getPostAuthor() { return post.getAuthor(); }
            @Override public String getPostTitle() { return post.getTitle(); }
            @Override public Long getPostId() { return postId; } // ✅ NEW
            @Override public String getReferenceType() { return "COMMUNITY"; } // ✅ NEW
        });
    }


    public List<CommentDto> getStateNewsComments(Long newsId) {
        return buildCommentTree(commentRepository.findByStateNewsIdOrderByCreatedAtAsc(newsId));
    }
    public List<CommentDto> getEventComments(Long eventId) {
        return buildCommentTree(commentRepository.findByEventIdOrderByCreatedAtAsc(eventId));
    }
    public List<CommentDto> getJobComments(Long jobId) {
        return buildCommentTree(commentRepository.findByJobIdOrderByCreatedAtAsc(jobId));
    }
    public List<CommentDto> getCommunityPostComments(Long postId) {
        return buildCommentTree(commentRepository.findByCommunityPostIdOrderByCreatedAtAsc(postId));
    }
    public List<CommentDto> getCommentsForPost(String postType, Long postId) {
        switch (postType.toLowerCase()) {
            case "state-news": return getStateNewsComments(postId);
            case "event": return getEventComments(postId);
            case "job": return getJobComments(postId);
            case "community": return getCommunityPostComments(postId);
            case "property": return buildCommentTree(commentRepository.findByPropertyIdOrderByCreatedAtAsc(postId));
            default: throw new IllegalArgumentException("Invalid post type: " + postType);
        }
    }

    public CommentDto updateComment(Long commentId, CreateCommentRequest request, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new SecurityException("You are not authorized to update this comment");
        }
        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);
        log.info("Updated comment with ID: {} by user: {}", commentId, currentUser.getEmail());
        return mapCommentToDto(updatedComment);
    }

    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

        boolean isCommentOwner = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isPostOwner = false;

        if (comment.getStateNewsId() != null) {
            isPostOwner = stateNewsRepository.findById(comment.getStateNewsId()).map(p -> p.getAuthor().getId().equals(currentUser.getId())).orElse(false);
        } else if (comment.getEventId() != null) {
            isPostOwner = eventRepository.findById(comment.getEventId()).map(p -> p.getAuthor().getId().equals(currentUser.getId())).orElse(false);
        } else if (comment.getJobId() != null) {
            isPostOwner = jobRepository.findById(comment.getJobId()).map(p -> p.getAuthor().getId().equals(currentUser.getId())).orElse(false);
        } else if (comment.getCommunityPostId() != null) {
            isPostOwner = communityPostRepository.findById(comment.getCommunityPostId()).map(p -> p.getAuthor().getId().equals(currentUser.getId())).orElse(false);
        } else if (comment.getPropertyId() != null) {
            // isPostOwner = propertyRepository.findById(comment.getPropertyId()).map(p -> p.getAuthor().getId().equals(currentUser.getId())).orElse(false);
        }
        if (!isCommentOwner && !isPostOwner) {
            throw new SecurityException("You are not authorized to delete this comment");
        }
        commentRepository.delete(comment);
        log.info("Deleted comment with ID: {} by user: {}", commentId, currentUser.getEmail());
    }
}
