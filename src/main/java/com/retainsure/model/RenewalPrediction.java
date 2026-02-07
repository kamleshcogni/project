package com.retainsure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenewalPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long predictionId;

    private Long customerId;
    private Long policyId;
    private Double renewalProbability;
    private Integer riskScore;
    private String predictionDate;
}