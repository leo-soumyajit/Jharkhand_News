package com.soumyajit.jharkhand_project.repository;

import com.soumyajit.jharkhand_project.entity.BannerAd;
import com.soumyajit.jharkhand_project.entity.BannerAd.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BannerAdRepository extends JpaRepository<BannerAd, Long> {


    List<BannerAd> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Status status, LocalDateTime current1, LocalDateTime current2);
}
