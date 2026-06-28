package com.re.badmintonsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "court_images")
@Getter
@Setter
@NoArgsConstructor
public class CourtImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "public_id", length = 255)
    private String publicId;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
