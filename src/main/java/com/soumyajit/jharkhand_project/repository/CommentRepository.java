package com.soumyajit.jharkhand_project.repository;

import com.soumyajit.jharkhand_project.entity.Comment;
import com.soumyajit.jharkhand_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDistrictNewsIdOrderByCreatedAtDesc(Long districtNewsId);
    List<Comment> findByEventIdOrderByCreatedAtDesc(Long eventId);
    List<Comment> findByJobIdOrderByCreatedAtDesc(Long jobId);
    List<Comment> findByCommunityPostIdOrderByCreatedAtDesc(Long communityPostId);
    void deleteByCommunityPostId(Long communityPostId);
    void deleteByEventId(Long eventId);
    long countByAuthor(User author);

}
