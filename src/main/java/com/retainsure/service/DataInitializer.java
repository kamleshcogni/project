package com.retainsure.service;

import com.retainsure.model.*;
import com.retainsure.repository.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
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
        // Prevent duplicates on restart
        if (userRepository.count() > 0 || customerRepository.count() > 0) {
            return;
        }

        // Admin
        userRepository.save(new User(null, "admin", encoder.encode("admin123"), Role.ROLE_ADMIN));

        List<SeedCustomer> seeds = List.of(
                new SeedCustomer("Kamlesh Sharma", "+91-9000000001", "kamlesh.sharma@example.com", "LOW"),
                new SeedCustomer("Aarav Mehta",   "+91-9000000002", "aarav.mehta@example.com",   "LOW"),
                new SeedCustomer("Diya Patel",    "+91-9000000003", "diya.patel@example.com",    "LOW"),
                new SeedCustomer("Rohan Verma",   "+91-9000000004", "rohan.verma@example.com",   "LOW"),
                new SeedCustomer("Neha Iyer",     "+91-9000000005", "neha.iyer@example.com",     "MEDIUM"),
                new SeedCustomer("Sana Khan",     "+91-9000000006", "sana.khan@example.com",     "MEDIUM"),
                new SeedCustomer("Vikram Singh",  "+91-9000000007", "vikram.singh@example.com",  "MEDIUM"),
                new SeedCustomer("Ananya Rao",    "+91-9000000008", "ananya.rao@example.com",    "HIGH"),
                new SeedCustomer("Kunal Jain",    "+91-9000000009", "kunal.jain@example.com",    "HIGH"),
                new SeedCustomer("Meera Nair",    "+91-9000000010", "meera.nair@example.com",    "HIGH")
        );

        Random rnd = new Random(42);
        LocalDate today = LocalDate.now();

        for (SeedCustomer s : seeds) {
            String username = toUsername(s.name());

            // 1) User
            User u = userRepository.save(
                    new User(null, username, encoder.encode("password"), Role.ROLE_CUSTOMER)
            );

            // 2) Customer (requires you added userId to Customer)
            Customer customer = customerRepository.save(
                    new Customer(null, u.getId(), s.name(), s.phone(), s.email(), s.riskLevel())
            );

            // 3) Policies: 2–6 per customer
            int policyCount = 2 + rnd.nextInt(5); // 2..6
            for (int i = 1; i <= policyCount; i++) {
                String type = (i % 2 == 0) ? "MOTOR" : "HEALTH";

                boolean active = rnd.nextBoolean();
                Policy.PolicyStatus status = active ? Policy.PolicyStatus.ACTIVE : Policy.PolicyStatus.EXPIRED;

                LocalDate start = today.minusMonths(6 + rnd.nextInt(18));
                LocalDate end = active
                        ? today.plusDays(15 + rnd.nextInt(180))
                        : today.minusDays(15 + rnd.nextInt(180));

                Policy policy = policyRepository.save(new Policy(
                        null,
                        customer.getCustomerId(),
                        type,
                        start.toString(),
                        end.toString(),
                        status
                ));

                // 4) Reminders: 1–3 per policy
                int reminderCount = 1 + rnd.nextInt(3);
                for (int r = 1; r <= reminderCount; r++) {
                    Reminder.ReminderStatus rStatus =
                            (r % 2 == 0) ? Reminder.ReminderStatus.RESPONDED : Reminder.ReminderStatus.SENT;

                    LocalDate sent = active ? end.minusDays(10 + rnd.nextInt(25)) : today.minusDays(rnd.nextInt(60));

                    reminderRepository.save(new Reminder(
                            null,
                            customer.getCustomerId(),
                            policy.getPolicyId(),
                            (type.equals("HEALTH") ? "Health" : "Motor") + " policy reminder #" + r,
                            sent.toString(),
                            rStatus
                    ));
                }

                // 5) Predictions: create 1 prediction per policy (riskScore aligned with customer riskLevel)
                int riskScore = switch (s.riskLevel()) {
                    case "HIGH" -> 70 + rnd.nextInt(31);      // 70..100
                    case "MEDIUM" -> 40 + rnd.nextInt(30);    // 40..69
                    default -> 10 + rnd.nextInt(30);          // 10..39
                };

                double renewalProb = switch (s.riskLevel()) {
                    case "HIGH" -> 0.10 + rnd.nextDouble() * 0.30;    // 0.10..0.40
                    case "MEDIUM" -> 0.40 + rnd.nextDouble() * 0.30;  // 0.40..0.70
                    default -> 0.70 + rnd.nextDouble() * 0.25;        // 0.70..0.95
                };

                predictionRepository.save(new RenewalPrediction(
                        null,
                        customer.getCustomerId(),
                        policy.getPolicyId(),
                        renewalProb,
                        riskScore,
                        today.plusDays(1 + rnd.nextInt(90)).toString()
                ));
            }
        }

        // Admin data for analytics pages
        campaignRepository.save(new Campaign(null, "Year-End Health Offer", "EXPIRING_SOON", "2025-12-15", "2026-01-15", "ACTIVE"));
        campaignRepository.save(new Campaign(null, "Motor Renewal Boost", "MOTOR", "2026-02-01", "2026-03-01", "SCHEDULED"));

        reportRepository.save(new RetentionReport(null, 72.0, 8.0, 45.0, "2025-12-31"));
        reportRepository.save(new RetentionReport(null, 78.0, 6.0, 55.0, "2026-01-31"));
    }

    private static String toUsername(String fullName) {
        return fullName.trim().toLowerCase().replaceAll("\\s+", ".");
    }

    private record SeedCustomer(String name, String phone, String email, String riskLevel) {}
}