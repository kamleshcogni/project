package com.retainsure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    // ✅ link to User
    @Column(nullable = false, unique = true)
    private Long userId;

    private String name;
    private String phone;
    private String email;
    private String riskLevel;
}