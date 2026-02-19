package com.retainsure.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campaignId;

    /**
     * Optional: to support frontend codes like C-001.
     */
    @Column(unique = false)
    private String campaignCode;

    private String campaignName;

    /**
     * Store as string to support both enum-like targets and free-form segments.
     */
    private String target;

    /**
     * Store numeric percent; frontend can display as string via DTO if needed.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal discountPercent;

    private String startDate; // keep string for compatibility with existing style
    private String endDate;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (modifiedAt == null) modifiedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}
