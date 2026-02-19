package com.retainsure.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "renewal_predictions",
        indexes = {
                @Index(name = "idx_predictions_customerId", columnList = "customerId"),
                @Index(name = "idx_predictions_policyId", columnList = "policyId")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenewalPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long predictionId;

    private Long customerId;
    private Long policyId;

    /**
     * Existing backend field (string). Keep it.
     */
    private String policyType;

    /**
     * Existing backend field.
     */
    private Double renewalProbability;

    /**
     * Existing backend field.
     */
    private String riskLevel;

    /**
     * Existing backend field.
     */
    private LocalDateTime createdAt;

    // ===== Added for admin risk dashboard compatibility =====

    private Integer riskScore; // 0-100

    private String renewalDate; // ISO string

    @Enumerated(EnumType.STRING)
    private RenewalStatus renewalStatus; // PENDING/RENEWED/EXPIRED

    private Boolean flagged;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (flagged == null) flagged = Boolean.FALSE;
        if (renewalStatus == null) renewalStatus = RenewalStatus.PENDING;
    }

    // Compatibility method for older service code expecting predictionDate
    public String getPredictionDate() {
        // Prefer renewalDate if you have it
        if (this.renewalDate != null && !this.renewalDate.isBlank()) {
            return this.renewalDate;
        }
        // Fall back to createdAt if present
        if (this.createdAt != null) {
            return this.createdAt.toString();
        }
        return null;
    }
}