package com.soumyajit.jharkhand_project.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean subscribed = true;
}
