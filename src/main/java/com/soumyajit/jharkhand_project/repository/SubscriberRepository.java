package com.soumyajit.jharkhand_project.repository;

import com.soumyajit.jharkhand_project.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    List<Subscriber> findBySubscribedTrue();
    Optional<Subscriber> findByEmail(String email);
}
