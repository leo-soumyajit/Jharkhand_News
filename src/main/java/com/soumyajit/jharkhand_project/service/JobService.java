package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.CommentDto;
import com.soumyajit.jharkhand_project.dto.CreateJobRequest;
import com.soumyajit.jharkhand_project.dto.JobDto;
import com.soumyajit.jharkhand_project.entity.Comment;
import com.soumyajit.jharkhand_project.entity.Job;
import com.soumyajit.jharkhand_project.entity.PostStatus;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.repository.CommentRepository;
import com.soumyajit.jharkhand_project.repository.JobRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Cacheable(value = "jobs", key = "'approved'")
    public List<JobDto> getApprovedJobs() {
        List<Job> jobs = jobRepository.findByStatusOrderByCreatedAtDesc(PostStatus.APPROVED);
        return jobs.stream()
                .map(job -> modelMapper.map(job, JobDto.class))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"jobs", "recent-jobs"}, allEntries = true)
    public JobDto createJob(CreateJobRequest request, List<MultipartFile> images, User author) {
        // Validate required fields
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required");
        }



        Job job = modelMapper.map(request, Job.class);
        job.setAuthor(author);

        PostStatus status = author.getRole().equals(User.Role.ADMIN)
                ? PostStatus.APPROVED
                : PostStatus.PENDING;

        job.setStatus(status);

        List<String> imageUrls = cloudinaryService.uploadImages(images);
        job.setImageUrls(imageUrls);

        Job savedJob = jobRepository.save(job);
        log.info("Created job with ID: {} by user: {}", savedJob.getId(), author.getEmail());

        return modelMapper.map(savedJob, JobDto.class);
    }



    @CacheEvict(value = {"jobs", "recent-jobs"}, allEntries = true)
    public JobDto approveJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found with ID: " + jobId));

        job.setStatus(PostStatus.APPROVED);
        Job savedJob = jobRepository.save(job);
        log.info("Approved job with ID: {}", jobId);

        // ✅ UPDATED: Notify the job poster about approval with reference
        notificationService.notifyUser(
                job.getAuthor().getId(),
                "Your job posting '" + job.getTitle() + "' has been approved!",
                jobId,            // Reference ID
                "JOB"             // Reference Type
        );

        return modelMapper.map(savedJob, JobDto.class);
    }



    public List<JobDto> getPendingJobs() {
        List<Job> jobs = jobRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PENDING);
        return jobs.stream()
                .map(job -> modelMapper.map(job, JobDto.class))
                .collect(Collectors.toList());
    }

    public JobDto getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job not found with ID: " + id));

        JobDto dto = modelMapper.map(job, JobDto.class);

        List<Comment> comments = commentRepository.findByJobIdOrderByCreatedAtAsc(id);
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());

        dto.setComments(commentDtos);
        return dto;
    }

    @CacheEvict(value = {"jobs", "recent-jobs"}, allEntries = true)
    public void deleteJob(Long jobId, User user) {
        // Find the job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found with ID: " + jobId));

        // Security check - only admin or job owner can delete
        boolean isAdmin = user.getRole().equals(User.Role.ADMIN);
        boolean isOwner = job.getAuthor().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Unauthorized: You can only delete your own jobs");
        }

        // Delete the job
        jobRepository.delete(job);

        log.info("Job deleted with ID: {} by user: {} ({})",
                jobId,
                user.getEmail(),
                isAdmin ? "ADMIN" : "OWNER"
        );
    }

    @Cacheable(value = "recent-jobs", key = "#days")
    public List<JobDto> getRecentJobs(int days) {
        // Calculate date threshold (last N days)
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(days);

        List<Job> recentJobs = jobRepository
                .findByStatusAndCreatedAtAfterOrderByCreatedAtDesc(PostStatus.APPROVED, dateThreshold);

        return recentJobs.stream()
                .map(job -> modelMapper.map(job, JobDto.class))
                .collect(Collectors.toList());
    }


}
