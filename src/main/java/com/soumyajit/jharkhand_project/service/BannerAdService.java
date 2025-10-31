package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.BannerAdDto;
import com.soumyajit.jharkhand_project.entity.BannerAd;
import com.soumyajit.jharkhand_project.entity.BannerAd.Status;
import com.soumyajit.jharkhand_project.repository.BannerAdRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BannerAdService {

    private final BannerAdRepository bannerAdRepository;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;

    public BannerAdDto createBannerAd(BannerAdDto dto) {
        BannerAd entity = modelMapper.map(dto, BannerAd.class);
        // When created, status is ACTIVE by default
        entity.setStatus(Status.ACTIVE);
        BannerAd saved = bannerAdRepository.save(entity);
        return modelMapper.map(saved, BannerAdDto.class);
    }

    public List<BannerAdDto> getAllAds() {
        return bannerAdRepository.findAll()
                .stream()
                .map(ad -> modelMapper.map(ad, BannerAdDto.class))
                .collect(Collectors.toList());
    }


    @Transactional
    public List<BannerAdDto> getActiveAds() {
        LocalDateTime now = LocalDateTime.now();
        List<BannerAd> activeAds = bannerAdRepository.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                Status.ACTIVE, now, now);

        activeAds.forEach(ad -> {
            ad.setImpressionsCount(ad.getImpressionsCount() + 1);
        });

        bannerAdRepository.saveAll(activeAds);

        return activeAds.stream()
                .map(ad -> modelMapper.map(ad, BannerAdDto.class))
                .collect(Collectors.toList());
    }

    // Get active SMALL banners
    @Transactional
    public List<BannerAdDto> getActiveSmallAds() {
        LocalDateTime now = LocalDateTime.now();
        List<BannerAd> activeAds = bannerAdRepository.findByStatusAndSizeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                Status.ACTIVE, BannerAd.Size.SMALL, now, now);

        // Increment impression count
        activeAds.forEach(ad -> {
            ad.setImpressionsCount(ad.getImpressionsCount() + 1);
        });
        bannerAdRepository.saveAll(activeAds);

        return activeAds.stream()
                .map(ad -> modelMapper.map(ad, BannerAdDto.class))
                .collect(Collectors.toList());
    }

    // Get active LARGE banners
    @Transactional
    public List<BannerAdDto> getActiveLargeAds() {
        LocalDateTime now = LocalDateTime.now();
        List<BannerAd> activeAds = bannerAdRepository.findByStatusAndSizeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                Status.ACTIVE, BannerAd.Size.LARGE, now, now);

        // Increment impression count
        activeAds.forEach(ad -> {
            ad.setImpressionsCount(ad.getImpressionsCount() + 1);
        });
        bannerAdRepository.saveAll(activeAds);

        return activeAds.stream()
                .map(ad -> modelMapper.map(ad, BannerAdDto.class))
                .collect(Collectors.toList());
    }



    public BannerAdDto getBannerAdById(Long id) {
        BannerAd ad = bannerAdRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BannerAd not found with ID: " + id));
        return modelMapper.map(ad, BannerAdDto.class);
    }

    public BannerAdDto updateBannerAd(Long id, BannerAdDto dto) {
        BannerAd ad = bannerAdRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BannerAd not found with ID: " + id));


        ad.setTitle(dto.getTitle());
        ad.setDestinationUrl(dto.getDestinationUrl());
        ad.setStartDate(dto.getStartDate());
        ad.setEndDate(dto.getEndDate());


        if (dto.getStatus() != null) {
            ad.setStatus(Status.valueOf(dto.getStatus()));
        }

        BannerAd saved = bannerAdRepository.save(ad);
        return modelMapper.map(saved, BannerAdDto.class);
    }


    public BannerAdDto changeStatus(Long id, Status status) {
        BannerAd ad = bannerAdRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BannerAd not found with ID: " + id));
        ad.setStatus(status);
        BannerAd saved = bannerAdRepository.save(ad);
        return modelMapper.map(saved, BannerAdDto.class);
    }


    public void deleteBannerAd(Long id) {
        BannerAd ad = bannerAdRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BannerAd not found with ID: " + id));

        if (ad.getPublicId() != null && !ad.getPublicId().isEmpty()) {
            cloudinaryService.deleteImage(ad.getPublicId());
        }

        bannerAdRepository.delete(ad);
    }
}
