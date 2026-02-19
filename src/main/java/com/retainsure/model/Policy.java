package com.retainsure.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Entity
@Table(
        name = "policies",
        indexes = {
                @Index(name = "idx_policies_customerId", columnList = "customerId"),
                @Index(name = "idx_policies_policyNumber", columnList = "policyNumber"),
                @Index(name = "idx_policies_category", columnList = "category")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;

    /**
     * Existing field used by your backend repositories/services.
     */
    private Long customerId;

    /**
     * Existing field currently used in your backend (e.g. "HEALTH", "MOTOR").
     * Keep it to avoid breaking code, but new code should prefer {@link #category}.
     */
    private String policyType;

    private String startDate; // keep as String to avoid breaking existing code
    private String endDate;   // keep as String to avoid breaking existing code

    @Enumerated(EnumType.STRING)
    private PolicyStatus status;

    // ===== Added to match frontend shapes (Admin + Customer) =====

    /**
     * Customer dashboard uses policyNumber.
     * Make it unique if you want, but leaving nullable is safest for existing data.
     */
    @Column(unique = false)
    private String policyNumber;

    /**
     * Admin uses category as enum: HEALTH/MOTOR/LIFE/TRAVEL
     */
    @Enumerated(EnumType.STRING)
    private PolicyCategory category;

    /**
     * Admin: policy_name, Customer: name
     */
    private String policyName;

    /**
     * Admin: amount, Customer: insuredValue (you can map DTO accordingly)
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Customer: premium
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal premium;

    /**
     * Customer: coverage (string)
     */
    private String coverage;

    /**
     * Customer: insuredValue (optional). If you don't want a second value,
     * you can keep it null and use 'amount' as insured value in DTO mapping.
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal insuredValue;

    /**
     * Customer: notes
     */
    @Column(length = 2000)
    private String notes;

    /**
     * Admin: renewal_date, Customer: renewalDate
     */
    private String renewalDate;

    /**
     * Admin: created_at, modified_at
     */
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