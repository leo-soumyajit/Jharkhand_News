// In: com.soumyajit.jharkhand_project.repository.CommentRepository.java

package com.soumyajit.jharkhand_project.repository;

import com.soumyajit.jharkhand_project.entity.Comment;
import com.soumyajit.jharkhand_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // MODIFIED: Changed to Asc for easier tree building
    List<Comment> findByDistrictNewsIdOrderByCreatedAtAsc(Long districtNewsId);
    List<Comment> findByEventIdOrderByCreatedAtAsc(Long eventId);
    List<Comment> findByJobIdOrderByCreatedAtAsc(Long jobId);
    List<Comment> findByCommunityPostIdOrderByCreatedAtAsc(Long communityPostId);
    List<Comment> findByPropertyIdOrderByCreatedAtAsc(Long propertyId); // Modified this too

    void deleteByCommunityPostId(Long communityPostId);
    void deleteByEventId(Long eventId);
    long countByAuthor(User author);
    void deleteByPropertyId(Long propertyId);
}
