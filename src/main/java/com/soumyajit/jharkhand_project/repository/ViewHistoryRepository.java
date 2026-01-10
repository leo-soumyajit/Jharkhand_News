package com.soumyajit.jharkhand_project.repository;

import com.soumyajit.jharkhand_project.entity.ViewHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    Optional<ViewHistory> findByUserIdAndContentTypeAndContentId(
            Long userId, ViewHistory.ContentType contentType, Long contentId);

    Page<ViewHistory> findByUserIdAndContentTypeOrderByViewedAtDesc(
            Long userId, ViewHistory.ContentType contentType, Pageable pageable);

    @Modifying
    @Query("DELETE FROM ViewHistory vh WHERE vh.user.id = :userId AND vh.contentType = :contentType AND vh.contentId = :contentId")
    void deleteByUserIdAndContentTypeAndContentId(
            @Param("userId") Long userId,
            @Param("contentType") ViewHistory.ContentType contentType,
            @Param("contentId") Long contentId);

    @Modifying
    @Query("DELETE FROM ViewHistory vh WHERE vh.user.id = :userId AND vh.contentType = :contentType")
    void deleteByUserIdAndContentType(
            @Param("userId") Long userId,
            @Param("contentType") ViewHistory.ContentType contentType);

    Long countByUserIdAndContentType(
            Long userId, ViewHistory.ContentType contentType);
}
