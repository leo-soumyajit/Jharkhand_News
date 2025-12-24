package com.soumyajit.jharkhand_project.repository;

import com.soumyajit.jharkhand_project.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByNameIgnoreCase(String name);
    Optional<State> findByName(String name);
    boolean existsByName(String name);
    List<State> findByNameContaining(String name);
}
