package com.soumyajit.jharkhand_project.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContentDto {
    private List<DistrictNewsDto> districtNews;
    private List<EventDto> events;
    private List<JobDto> jobs;
    private List<CommunityPostDto> communityPosts;
    private long totalElements;
}

