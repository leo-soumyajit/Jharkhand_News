package com.soumyajit.jharkhand_project.repository;


import com.soumyajit.jharkhand_project.entity.CommunityPost;
import com.soumyajit.jharkhand_project.entity.PostStatus;
import com.soumyajit.jharkhand_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    List<CommunityPost> findByStatusOrderByCreatedAtDesc(PostStatus status);
    List<CommunityPost> findByAuthorOrderByCreatedAtDesc(User author);

    List<CommunityPost> findByStatusAndCreatedAtAfterOrderByCreatedAtDesc(PostStatus status, LocalDateTime after);

    long countByAuthor(User author);


}