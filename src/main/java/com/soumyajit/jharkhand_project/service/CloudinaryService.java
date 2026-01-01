package com.soumyajit.jharkhand_project.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Data
    @AllArgsConstructor
    public static class CloudinaryUploadResult {
        private String url;
        private String publicId;
    }

    public CloudinaryUploadResult uploadImageWithPublicId(MultipartFile file) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader()
                    .upload(file.getBytes(),
                            Map.of("resource_type", "auto",
                                    "folder", "jharkhand_news",
                                    "quality", "auto:good",      // - 40-50% compression
                                    "fetch_format", "auto"));    // WebP for modern browsers

            String url = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();

            log.info("Uploaded image to Cloudinary: {} -> {}", publicId, url);
            return new CloudinaryUploadResult(url, publicId);

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    public String uploadImage(MultipartFile file) {
        return uploadImageWithPublicId(file).getUrl();
    }

    public List<CloudinaryUploadResult> uploadImagesWithPublicIds(List<MultipartFile> files) {
        return files.stream()
                .map(this::uploadImageWithPublicId)
                .collect(Collectors.toList());
    }

    public List<String> uploadImages(List<MultipartFile> files) {
        return files.stream()
                .map(this::uploadImage)
                .collect(Collectors.toList());
    }

    //dlt single image
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            log.warn("Attempted to delete image with null or empty public_id");
            return;
        }

        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = result.get("result").toString();

            if ("ok".equals(resultStatus)) {
                log.info("Successfully deleted image from Cloudinary: {}", publicId);
            } else {
                log.warn("Cloudinary deletion returned status: {} for public_id: {}",
                        resultStatus, publicId);
            }
        } catch (Exception e) {
            log.error("Failed to delete image from Cloudinary: {}", publicId, e);
        }
    }

    //dlt multiple images
    public void deleteImages(List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) {
            log.info("No images to delete from Cloudinary");
            return;
        }

        log.info("Deleting {} images from Cloudinary", publicIds.size());

        for (String publicId : publicIds) {
            deleteImage(publicId);
        }
    }
}
