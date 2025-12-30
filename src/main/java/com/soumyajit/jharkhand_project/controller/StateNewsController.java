package com.soumyajit.jharkhand_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.CreateStateNewsRequest;
import com.soumyajit.jharkhand_project.dto.StateNewsDto;
import com.soumyajit.jharkhand_project.dto.UpdateStateNewsRequest;
import com.soumyajit.jharkhand_project.entity.NewsCategory;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.StateNewsService;
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
import com.soumyajit.jharkhand_project.entity.NewsCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;


import java.util.List;

@RestController
@RequestMapping("/state-news")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
@Slf4j
public class StateNewsController {

    private final StateNewsService stateNewsService;
    private final ObjectMapper objectMapper;

    //give all
    @GetMapping("/{stateName}")
    public ResponseEntity<ApiResponse<Page<StateNewsDto>>> getNewsByState(
            @PathVariable String stateName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<StateNewsDto> newsPage = stateNewsService.getNewsByState(stateName, page, size);
            return ResponseEntity.ok(ApiResponse.success("News retrieved successfully", newsPage));
        } catch (Exception e) {
            log.error("Error retrieving news for state: {}", stateName, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve news"));
        }
    }



    //give recent news
    @GetMapping("/{stateName}/recent")
    public ResponseEntity<ApiResponse<List<StateNewsDto>>> getRecentNewsByState(  // ✅ FIXED method name
                                                                                  @PathVariable String stateName,
                                                                                  @RequestParam(defaultValue = "3") int days) {
        try {
            List<StateNewsDto> recentNews = stateNewsService.getRecentNewsByState(stateName, days);  // ✅ FIXED
            return ResponseEntity.ok(ApiResponse.success("Recent news retrieved successfully", recentNews));
        } catch (Exception e) {
            log.error("Error retrieving recent news for state: {}", stateName, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent news"));
        }
    }

    //get news by id
    @GetMapping("/details/{id}")
    public ResponseEntity<ApiResponse<StateNewsDto>> getNewsById(@PathVariable Long id) {
        try {
            StateNewsDto news = stateNewsService.getNewsById(id);
            return ResponseEntity.ok(ApiResponse.success("News details retrieved successfully", news));
        } catch (Exception e) {
            log.error("Error retrieving news with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    //create news
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('REPORTER')")
    public ResponseEntity<ApiResponse<StateNewsDto>> createStateNews(  // ✅ FIXED method name
                                                                       @RequestPart("news") String newsJson,
                                                                       @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                                                       Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            ObjectMapper mapper = new ObjectMapper();
            CreateStateNewsRequest request = mapper.readValue(newsJson, CreateStateNewsRequest.class);

            StateNewsDto news = stateNewsService.createStateNews(request, images, user);  // ✅ FIXED
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("State news created successfully", news));
        } catch (Exception e) {
            log.error("Error creating state news", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create state news"));
        }
    }

    //update news
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REPORTER')")
    public ResponseEntity<ApiResponse<StateNewsDto>> updateStateNews(  // ✅ FIXED method name
                                                                       @PathVariable Long id,
                                                                       @RequestBody UpdateStateNewsRequest request,
                                                                       Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            StateNewsDto updatedNews = stateNewsService.updateStateNews(id, request, user);  // ✅ FIXED
            return ResponseEntity.ok(ApiResponse.success("State news updated successfully", updatedNews));
        } catch (Exception e) {
            log.error("Error updating state news with ID: {}", id, e);
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only update your own news articles"));
            }
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update state news"));
        }
    }

    //delete news
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REPORTER')")
    public ResponseEntity<ApiResponse<String>> deleteStateNews(  // ✅ FIXED method name
                                                                 @PathVariable Long id,
                                                                 Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            stateNewsService.deleteStateNews(id, user);  // ✅ FIXED
            return ResponseEntity.ok(ApiResponse.success("State news deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting state news with ID: {}", id, e);
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only delete your own news articles"));
            }
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete state news"));  // ✅ FIXED message
        }
    }

    @GetMapping(value = "/statenews/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getSharePage(@PathVariable Long id) {
        try {
            StateNewsDto news = stateNewsService.getNewsById(id);

            String title = news.getTitle().replaceAll("<[^>]*>", "").trim();
            String description = news.getContent().replaceAll("<[^>]*>", "").trim();
            if (description.length() > 200) {
                description = description.substring(0, 200) + "...";
            }

            String image = (news.getImageUrls() != null && !news.getImageUrls().isEmpty())
                    ? news.getImageUrls().get(0)
                    : "https://jharkhandbiharupdates.com/default-image.jpg";

            String newsUrl = "https://jharkhandbiharupdates.com/localnews/details/" + id;

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




    @GetMapping("/{stateName}/category/{category}")
    public ResponseEntity<ApiResponse<List<StateNewsDto>>> getNewsByStateAndCategory(
            @PathVariable String stateName,
            @PathVariable NewsCategory category) {
        try {
            List<StateNewsDto> news = stateNewsService.getNewsByStateAndCategory(stateName, category);
            return ResponseEntity.ok(ApiResponse.success("News retrieved successfully", news));
        } catch (Exception e) {
            log.error("Error retrieving news for state: {} and category: {}", stateName, category, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve news"));
        }
    }

    @GetMapping("/{stateName}/category/{category}/recent")
    public ResponseEntity<ApiResponse<List<StateNewsDto>>> getRecentNewsByStateAndCategory(
            @PathVariable String stateName,
            @PathVariable NewsCategory category,
            @RequestParam(defaultValue = "3") int days) {
        try {
            List<StateNewsDto> news = stateNewsService.getRecentNewsByStateAndCategory(stateName, category, days);
            return ResponseEntity.ok(ApiResponse.success("Recent news retrieved successfully", news));
        } catch (Exception e) {
            log.error("Error retrieving recent news for state: {} and category: {}", stateName, category, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent news"));
        }
    }


    private String buildShareHtml(String title, String description, String image, String newsUrl) {
        String escapedTitle = title.replace("\"", "&quot;").replace("'", "&#39;");
        String escapedDescription = description.replace("\"", "&quot;").replace("'", "&#39;");

        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>" + escapedTitle + "</title>" +
                "<meta property='og:type' content='article'>" +
                "<meta property='og:title' content='" + escapedTitle + "'>" +
                "<meta property='og:description' content='" + escapedDescription + "'>" +
                "<meta property='og:image' content='" + image + "'>" +
                "<meta property='og:url' content='" + newsUrl + "'>" +
                "<meta property='og:site_name' content='Jharkhand Bihar Updates'>" +
                "<meta name='twitter:card' content='summary_large_image'>" +
                "<meta name='twitter:title' content='" + escapedTitle + "'>" +
                "<meta name='twitter:description' content='" + escapedDescription + "'>" +
                "<meta name='twitter:image' content='" + image + "'>" +
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
