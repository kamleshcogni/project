package com.retainsure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetentionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    private Double renewalRate;
    private Double churnRate;
    private Double campaignEffectiveness;
    private String generatedDate;
}