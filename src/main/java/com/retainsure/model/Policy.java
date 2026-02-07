package com.retainsure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;

    private Long customerId;
    private String policyType;
    private String startDate;
    private String endDate;

    @Enumerated(EnumType.STRING)
    private PolicyStatus status;

    public enum PolicyStatus {
        ACTIVE, EXPIRED, RENEWED
    }
}