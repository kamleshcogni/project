package com.retainsure.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "reminders",
        indexes = {
                @Index(name = "idx_reminders_customerId", columnList = "customerId"),
                @Index(name = "idx_reminders_policyId", columnList = "policyId")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reminderId;

    private Long customerId;
    private Long policyId;

    /**
     * Existing backend field.
     * Admin UI calls this "trigger". We'll keep message and also add trigger.
     */
    @Column(length = 2000)
    private String message;

    /**
     * Existing backend field.
     * Admin UI calls this "date_sent". We'll keep sentDate and also add dateSent.
     */
    private String sentDate;

    @Enumerated(EnumType.STRING)
    private ReminderStatus status;

    // ===== Added to match frontend admin Reminder shape =====

    @Enumerated(EnumType.STRING)
    private PolicyCategory category;


    /**
     * Admin UI calls this trigger (message content).
     * If null, you can fall back to 'message' in mapping.
     */
    @Column(name = "trigger_message", length = 2000)
    private String trigger;

    /**
     * Admin optional enriched field
     */
    private Integer riskScore;

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