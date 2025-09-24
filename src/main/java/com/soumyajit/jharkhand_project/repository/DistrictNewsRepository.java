package com.soumyajit.jharkhand_project.repository;


import com.soumyajit.jharkhand_project.entity.District;
import com.soumyajit.jharkhand_project.entity.DistrictNews;
import com.soumyajit.jharkhand_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DistrictNewsRepository extends JpaRepository<DistrictNews, Long> {
    List<DistrictNews> findByDistrictAndPublishedTrueOrderByCreatedAtDesc(District district);
    List<DistrictNews> findByAuthorOrderByCreatedAtDesc(User author);

    @Query("SELECT dn FROM DistrictNews dn WHERE dn.district.name = :districtName AND dn.published = true ORDER BY dn.createdAt DESC")
    List<DistrictNews> findByDistrictNameAndPublishedTrue(@Param("districtName") String districtName);


    // NEW: Recent news within date range
    List<DistrictNews> findByDistrictAndPublishedTrueAndCreatedAtAfterOrderByCreatedAtDesc(
            District district,
            LocalDateTime after
    );
}
