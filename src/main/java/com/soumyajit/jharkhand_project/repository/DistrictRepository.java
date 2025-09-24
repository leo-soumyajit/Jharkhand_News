package com.soumyajit.jharkhand_project.repository;

import com.soumyajit.jharkhand_project.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    Optional<District> findByNameIgnoreCase(String name);
    Optional<District> findByName(String name);
    boolean existsByName(String name);
    List<District> findByNameContaining(String name);
}
