package com.retainsure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reminderId;

    private Long customerId;
    private Long policyId;

    @Column(length = 500)
    private String message;

    private String sentDate;

    @Enumerated(EnumType.STRING)
    private ReminderStatus status;

    public enum ReminderStatus {
        SENT, RESPONDED
    }
}