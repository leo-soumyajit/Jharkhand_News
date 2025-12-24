package com.soumyajit.jharkhand_project.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private long totalStateNews;
    private long totalEvents;
    private long totalJobs;
    private long totalCommunityPosts;
    private long totalComments;
//    private long totalNotifications;

    public long getTotalContent() {
        return totalStateNews + totalEvents + totalJobs + totalCommunityPosts;
    }
}

