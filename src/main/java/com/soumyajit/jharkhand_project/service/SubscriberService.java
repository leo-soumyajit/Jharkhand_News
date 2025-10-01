package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.entity.Subscriber;
import com.soumyajit.jharkhand_project.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;

    @Transactional
    public Subscriber subscribe(String email) {
        Optional<Subscriber> existing = subscriberRepository.findByEmail(email);
        if (existing.isPresent()) {
            Subscriber subscriber = existing.get();
            subscriber.setSubscribed(true);
            return subscriberRepository.save(subscriber);
        }
        Subscriber newSubscriber = new Subscriber();
        newSubscriber.setEmail(email);
        newSubscriber.setSubscribed(true);
        return subscriberRepository.save(newSubscriber);
    }

    @Transactional
    public void unsubscribe(String email) {
        subscriberRepository.findByEmail(email).ifPresent(subscriber -> {
            subscriber.setSubscribed(false);
            subscriberRepository.save(subscriber);
        });
    }
}
