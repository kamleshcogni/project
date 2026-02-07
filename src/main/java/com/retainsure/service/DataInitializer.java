package com.retainsure.service;

import com.retainsure.model.*;
import com.retainsure.repository.*;
import java.util.List;

import com.retainsure.model.*;
import com.retainsure.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PolicyRepository policyRepository;
    private final RenewalPredictionRepository predictionRepository;
    private final ReminderRepository reminderRepository;
    private final CampaignRepository campaignRepository;
    private final RetentionReportRepository reportRepository;
    private final PasswordEncoder encoder;

    public DataInitializer(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           PolicyRepository policyRepository,
                           RenewalPredictionRepository predictionRepository,
                           ReminderRepository reminderRepository,
                           CampaignRepository campaignRepository,
                           RetentionReportRepository reportRepository,
                           PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.policyRepository = policyRepository;
        this.predictionRepository = predictionRepository;
        this.reminderRepository = reminderRepository;
        this.campaignRepository = campaignRepository;
        this.reportRepository = reportRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(new User(null, "admin", encoder.encode("admin123"), Role.ROLE_ADMIN));
            userRepository.save(new User(null, "customer", encoder.encode("customer123"), Role.ROLE_CUSTOMER));
        }

        if (customerRepository.count() == 0) {
            Customer c1 = new Customer(null, "Aarav Sharma", "+91-9000000001", "aarav@example.com", "LOW");
            Customer c2 = new Customer(null, "Diya Patel", "+91-9000000002", "diya@example.com", "HIGH");

            customerRepository.saveAll(List.of(c1, c2));

            Policy p1 = new Policy(null, c1.getCustomerId(), "HEALTH", "2025-01-01", "2025-12-31", Policy.PolicyStatus.ACTIVE);
            Policy p2 = new Policy(null, c2.getCustomerId(), "MOTOR", "2024-12-01", "2025-11-30", Policy.PolicyStatus.EXPIRED);

            policyRepository.saveAll(List.of(p1, p2));

            predictionRepository.save(new RenewalPrediction(null, c1.getCustomerId(), p1.getPolicyId(), 0.78, 35, "2025-12-01"));
            predictionRepository.save(new RenewalPrediction(null, c2.getCustomerId(), p2.getPolicyId(), 0.22, 72, "2025-11-15"));

            reminderRepository.save(new Reminder(null, c1.getCustomerId(), p1.getPolicyId(), "Your policy renews soon", "2025-12-20", Reminder.ReminderStatus.SENT));
            reminderRepository.save(new Reminder(null, c2.getCustomerId(), p2.getPolicyId(), "Motor policy renewal reminder", "2025-11-25", Reminder.ReminderStatus.RESPONDED));

            campaignRepository.save(new Campaign(null, "Year-End Health Offer", "EXPIRING_SOON", "2025-12-15", "2026-01-15", "ACTIVE"));

            reportRepository.save(new RetentionReport(null, 72.0, 8.0, 45.0, "2025-12-31"));
        }
    }
}