package com.soumyajit.jharkhand_project.repository;

import com.soumyajit.jharkhand_project.entity.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // Get all login history for a specific user (paginated)
    Page<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId, Pageable pageable);

    // Get recent login history for a user
    List<LoginHistory> findTop10ByUserIdOrderByLoginTimeDesc(Long userId);

    // Count total logins for a user
    Long countByUserId(Long userId);
}
