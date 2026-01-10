package com.soumyajit.jharkhand_project.dto;

import com.soumyajit.jharkhand_project.entity.ViewHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewHistoryDto {
    private Long id;
    private ViewHistory.ContentType contentType;
    private Long contentId;
    private LocalDateTime viewedAt;
    private Object contentDetails;
}
