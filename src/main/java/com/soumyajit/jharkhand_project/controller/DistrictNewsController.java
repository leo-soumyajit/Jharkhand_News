package com.soumyajit.jharkhand_project.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.CreateDistrictNewsRequest;
import com.soumyajit.jharkhand_project.dto.DistrictNewsDto;
import com.soumyajit.jharkhand_project.dto.UpdateDistrictNewsRequest;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.DistrictNewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/district-news")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
@Slf4j
public class DistrictNewsController {

    private final DistrictNewsService districtNewsService;
    private final ObjectMapper objectMapper;

    //give all
    @GetMapping("/{districtName}")
    public ResponseEntity<ApiResponse<List<DistrictNewsDto>>> getNewsByDistrict(@PathVariable String districtName) {
        try {
            List<DistrictNewsDto> news = districtNewsService.getNewsByDistrict(districtName);
            return ResponseEntity.ok(ApiResponse.success("News retrieved successfully", news));
        } catch (Exception e) {
            log.error("Error retrieving news for district: {}", districtName, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve news"));
        }
    }

    //give 5 days
    @GetMapping("/{districtName}/recent")
    public ResponseEntity<ApiResponse<List<DistrictNewsDto>>> getRecentNewsByDistrict(
            @PathVariable String districtName,
            @RequestParam(defaultValue = "3") int days) {
        try {
            List<DistrictNewsDto> recentNews = districtNewsService.getRecentNewsByDistrict(districtName, days);
            return ResponseEntity.ok(ApiResponse.success("Recent news retrieved successfully", recentNews));
        } catch (Exception e) {
            log.error("Error retrieving recent news for district: {}", districtName, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent news"));
        }
    }


    //get news by id
    @GetMapping("/details/{id}")
    public ResponseEntity<ApiResponse<DistrictNewsDto>> getNewsById(@PathVariable Long id) {
        try {
            DistrictNewsDto news = districtNewsService.getNewsById(id);
            return ResponseEntity.ok(ApiResponse.success("News details retrieved successfully", news));
        } catch (Exception e) {
            log.error("Error retrieving news with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    //create news
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('REPORTER')")
    public ResponseEntity<ApiResponse<DistrictNewsDto>> createDistrictNews(
            @RequestPart("news") String newsJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            // Parse JSON string manually
            ObjectMapper mapper = new ObjectMapper();
            CreateDistrictNewsRequest request = mapper.readValue(newsJson, CreateDistrictNewsRequest.class);

            DistrictNewsDto news = districtNewsService.createDistrictNews(request, images, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("District news created successfully", news));
        } catch (Exception e) {
            log.error("Error creating district news", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create district news"));
        }
    }


    //update news
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REPORTER')")
    public ResponseEntity<ApiResponse<DistrictNewsDto>> updateDistrictNews(
            @PathVariable Long id,
            @RequestBody UpdateDistrictNewsRequest request, // Changed to @RequestBody (no multipart)
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            DistrictNewsDto updatedNews = districtNewsService.updateDistrictNews(id, request, user);
            return ResponseEntity.ok(ApiResponse.success("District news updated successfully", updatedNews));
        } catch (Exception e) {
            log.error("Error updating district news with ID: {}", id, e);
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only update your own news articles"));
            }
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update district news"));
        }
    }


    //delete news
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REPORTER')")
    public ResponseEntity<ApiResponse<String>> deleteDistrictNews(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            districtNewsService.deleteDistrictNews(id, user);
            return ResponseEntity.ok(ApiResponse.success("District news deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting district news with ID: {}", id, e);
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only delete your own news articles"));
            }
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete district news"));
        }
    }

    @GetMapping(value = "/localnews/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getSharePage(@PathVariable Long id) {
        try {
            // Use the same method as your existing controller
            DistrictNewsDto news = districtNewsService.getNewsById(id);

            // Clean HTML tags from title and content
            String title = news.getTitle().replaceAll("<[^>]*>", "").trim();
            String description = news.getContent().replaceAll("<[^>]*>", "").trim();
            if (description.length() > 200) {
                description = description.substring(0, 200) + "...";
            }

            // Get first image or use default
            String image = (news.getImageUrls() != null && !news.getImageUrls().isEmpty())
                    ? news.getImageUrls().get(0)
                    : "https://jharkhandbiharupdates.com/default-image.jpg";

            String newsUrl = "https://jharkhandbiharupdates.com/localnews/details/" + id;

            // Build HTML with meta tags
            String html = buildShareHtml(title, description, image, newsUrl);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);

        } catch (Exception e) {
            log.error("Error generating share page for news ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body("<html><body><h2>Error loading news</h2></body></html>");
        }
    }

    private String buildShareHtml(String title, String description, String image, String newsUrl) {
        // Escape quotes for HTML attributes
        String escapedTitle = title.replace("\"", "&quot;").replace("'", "&#39;");
        String escapedDescription = description.replace("\"", "&quot;").replace("'", "&#39;");

        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>" + escapedTitle + "</title>" +

                "<!-- Open Graph Meta Tags -->" +
                "<meta property='og:type' content='article'>" +
                "<meta property='og:title' content='" + escapedTitle + "'>" +
                "<meta property='og:description' content='" + escapedDescription + "'>" +
                "<meta property='og:image' content='" + image + "'>" +
                "<meta property='og:url' content='" + newsUrl + "'>" +
                "<meta property='og:site_name' content='Jharkhand Bihar Updates'>" +

                "<!-- Twitter Card Meta Tags -->" +
                "<meta name='twitter:card' content='summary_large_image'>" +
                "<meta name='twitter:title' content='" + escapedTitle + "'>" +
                "<meta name='twitter:description' content='" + escapedDescription + "'>" +
                "<meta name='twitter:image' content='" + image + "'>" +

                "<!-- Auto redirect for real users -->" +
                "<script>" +
                "setTimeout(function() { window.location.href='" + newsUrl + "'; }, 500);" +
                "</script>" +
                "</head>" +
                "<body style='font-family: Arial; text-align: center; padding: 50px;'>" +
                "<h2>Loading news...</h2>" +
                "<p>If you are not redirected, <a href='" + newsUrl + "'>click here</a></p>" +
                "</body>" +
                "</html>";
    }

}
