package com.retainsure.service;

import com.retainsure.model.*;
import com.retainsure.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public DataInitializer(
            UserRepository userRepository,
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

        // Admin user
        userRepository.save(new User(null, "kamlesh", encoder.encode("password"), Role.ROLE_ADMIN));

        List<SeedCustomer> seeds =
                List.of(
                        new SeedCustomer("Kamlesh Khatod", "+91-9000000001", "kamlesh.khatod@example.com", "LOW"),
                        new SeedCustomer("Aarav Mehta", "+91-9000000002", "aarav.mehta@example.com", "LOW"),
                        new SeedCustomer("Diya Patel", "+91-9000000003", "diya.patel@example.com", "LOW"),
                        new SeedCustomer("Rohan Verma", "+91-9000000004", "rohan.verma@example.com", "LOW"),
                        new SeedCustomer("Neha Iyer", "+91-9000000005", "neha.iyer@example.com", "MEDIUM"),
                        new SeedCustomer("Sana Khan", "+91-9000000006", "sana.khan@example.com", "MEDIUM"),
                        new SeedCustomer("Vikram Singh", "+91-9000000007", "vikram.singh@example.com", "MEDIUM"),
                        new SeedCustomer("Ananya Rao", "+91-9000000008", "ananya.rao@example.com", "HIGH"),
                        new SeedCustomer("Kunal Jain", "+91-9000000009", "kunal.jain@example.com", "HIGH"),
                        new SeedCustomer("Meera Nair", "+91-9000000010", "meera.nair@example.com", "HIGH"));

        Random rnd = new Random(42);
        LocalDate today = LocalDate.now();

        for (SeedCustomer s : seeds) {
            String username = toUsername(s.name());

            // 1) User (customer login)
            User u = userRepository.save(new User(null, username, encoder.encode("password"), Role.ROLE_CUSTOMER));

            // 2) Customer
            // IMPORTANT: If your User entity uses getUserId() (not getId()), switch to that.
            Long userId = safeGetUserId(u);

            Customer customer =
                    customerRepository.save(
                            new Customer(null, userId, s.name(), s.phone(), s.email(), s.riskLevel()));

            // 3) Policies: 2–6 per customer
            int policyCount = 2 + rnd.nextInt(5); // 2..6
            for (int i = 1; i <= policyCount; i++) {

                PolicyCategory category = switch (i % 2) {
                    case 0 -> PolicyCategory.MOTOR;
                    case 1 -> PolicyCategory.HEALTH;
                    default -> PolicyCategory.MOTOR; // unreachable, but keeps compiler happy in some setups
                };

                boolean active = rnd.nextBoolean();
                PolicyStatus status = active ? PolicyStatus.ACTIVE : PolicyStatus.EXPIRED;

                LocalDate start = today.minusMonths(6 + rnd.nextInt(18));
                LocalDate end =
                        active ? today.plusDays(15 + rnd.nextInt(180)) : today.minusDays(15 + rnd.nextInt(180));

                // Renewal date: for active policies, close to end date; for expired, equal to end date
                LocalDate renewalDate = end;

                String policyName = policyNameFor(category);
                String policyNumber = policyNumberFor(category, customer.getCustomerId(), i);

                BigDecimal insuredValue = insuredValueFor(category, rnd);
                BigDecimal amount = insuredValue; // keep amount aligned to insuredValue for admin screens
                BigDecimal premium = premiumFor(category, insuredValue, rnd);

                String coverage = coverageFor(category);
                String notes = notesFor(category);

                LocalDateTime createdAt = LocalDateTime.now().minusDays(10 + rnd.nextInt(60));
                LocalDateTime modifiedAt = createdAt.plusDays(rnd.nextInt(10));

                Policy policy = new Policy();
                policy.setCustomerId(customer.getCustomerId());

                // keep old backend fields populated too
                policy.setPolicyType(category.name()); // old code used "MOTOR"/"HEALTH"
                policy.setStartDate(start.toString());
                policy.setEndDate(end.toString());
                policy.setStatus(status);

                // new fields
                policy.setPolicyNumber(policyNumber);
                policy.setCategory(category);
                policy.setPolicyName(policyName);
                policy.setAmount(amount);
                policy.setPremium(premium);
                policy.setCoverage(coverage);
                policy.setInsuredValue(insuredValue);
                policy.setNotes(notes);
                policy.setRenewalDate(renewalDate.toString());
                policy.setCreatedAt(createdAt);
                policy.setModifiedAt(modifiedAt);

                policy = policyRepository.save(policy);

                // 4) Reminders: 1–3 per policy
                int reminderCount = 1 + rnd.nextInt(3);
                for (int r = 1; r <= reminderCount; r++) {

                    ReminderStatus rStatus =
                            switch (rnd.nextInt(4)) {
                                case 0 -> ReminderStatus.SCHEDULED;
                                case 1 -> ReminderStatus.SENT;
                                case 2 -> ReminderStatus.RESPONDED;
                                default -> ReminderStatus.FAILED;
                            };


                    // pick a send date around renewal/end date
                    LocalDate sent =
                            active ? renewalDate.minusDays(7 + rnd.nextInt(25)) : today.minusDays(rnd.nextInt(60));

                    String trigger =
                            (category == PolicyCategory.HEALTH ? "Health" : category.name())
                                    + " renewal reminder #"
                                    + r
                                    + " for "
                                    + policyNumber;

                    Reminder reminder = new Reminder();
                    reminder.setCustomerId(customer.getCustomerId());
                    reminder.setPolicyId(policy.getPolicyId());

                    // old fields (keep filled)
                    reminder.setMessage(trigger);
                    reminder.setSentDate(sent.toString());
                    reminder.setStatus(rStatus);

                    // new fields (admin)
                    reminder.setCategory(category);
                    reminder.setTrigger(trigger);
                    reminder.setRiskScore(riskScoreFor(s.riskLevel(), rnd));
                    reminder.setCreatedAt(LocalDateTime.now().minusDays(rnd.nextInt(30)));
                    reminder.setModifiedAt(LocalDateTime.now().minusDays(rnd.nextInt(10)));

                    reminderRepository.save(reminder);
                }

                // 5) Prediction: 1 per policy (aligned with customer riskLevel)
                int riskScore = riskScoreFor(s.riskLevel(), rnd);

                double renewalProb =
                        switch (s.riskLevel()) {
                            case "HIGH" -> 0.10 + rnd.nextDouble() * 0.30; // 0.10..0.40
                            case "MEDIUM" -> 0.40 + rnd.nextDouble() * 0.30; // 0.40..0.70
                            default -> 0.70 + rnd.nextDouble() * 0.25; // 0.70..0.95
                        };

                // Make prediction renewal date near policy renewal date
                String predRenewalDate = renewalDate.plusDays(rnd.nextInt(10)).toString();

                RenewalStatus renewalStatus =
                        status == PolicyStatus.EXPIRED ? RenewalStatus.EXPIRED : RenewalStatus.PENDING;

                RenewalPrediction pred = new RenewalPrediction();
                pred.setCustomerId(customer.getCustomerId());
                pred.setPolicyId(policy.getPolicyId());
                pred.setPolicyType(category.name()); // keep compatibility with old string field
                pred.setRenewalProbability(roundDouble(renewalProb, 2));
                pred.setRiskLevel(s.riskLevel());
                pred.setCreatedAt(LocalDateTime.now().minusDays(rnd.nextInt(25)));

                // new fields
                pred.setRiskScore(riskScore);
                pred.setRenewalDate(predRenewalDate);
                pred.setRenewalStatus(renewalStatus);
                pred.setFlagged("HIGH".equals(s.riskLevel()) && riskScore >= 85);

                predictionRepository.save(pred);
            }
        }

        // ===== Admin data for analytics pages =====
        // NOTE: This assumes you use the newer Campaign entity (campaignId, campaignCode, ...).
        // If your backend Campaign is different right now, paste your Campaign.java and I will adjust.
        Campaign c1 = new Campaign();
        c1.setCampaignCode("C-001");
        c1.setCampaignName("Year-End Health Offer");
        c1.setTarget("HIGH");
        c1.setDiscountPercent(new BigDecimal("10.00"));
        c1.setStartDate(today.minusDays(10).toString());
        c1.setEndDate(today.plusDays(25).toString());
        c1.setStatus(CampaignStatus.ACTIVE);
        c1.setCreatedAt(LocalDateTime.now().minusDays(15));
        c1.setModifiedAt(LocalDateTime.now().minusDays(5));
        campaignRepository.save(c1);

        Campaign c2 = new Campaign();
        c2.setCampaignCode("C-002");
        c2.setCampaignName("Motor Renewal Boost");
        c2.setTarget("MOTOR");
        c2.setDiscountPercent(new BigDecimal("7.50"));
        c2.setStartDate(today.plusDays(5).toString());
        c2.setEndDate(today.plusDays(35).toString());
        c2.setStatus(CampaignStatus.SCHEDULED);
        c2.setCreatedAt(LocalDateTime.now().minusDays(2));
        c2.setModifiedAt(LocalDateTime.now().minusDays(1));
        campaignRepository.save(c2);

        reportRepository.save(new RetentionReport(null, 72.0, 8.0, 45.0, today.minusMonths(1).withDayOfMonth(28).toString()));
        reportRepository.save(new RetentionReport(null, 78.0, 6.0, 55.0, today.withDayOfMonth(28).toString()));
    }

    private static String toUsername(String fullName) {
        return fullName.trim().toLowerCase().replaceAll("\\s+", ".");
    }

    private static String policyNameFor(PolicyCategory category) {
        return switch (category) {
            case MOTOR -> "Car Insurance";
            case HEALTH -> "Health Insurance";
        };
    }

    private static String policyNumberFor(PolicyCategory category, Long customerId, int i) {
        String prefix =
                switch (category) {
                    case MOTOR -> "RS-MTR";
                    case HEALTH -> "RS-HLT";
                };
        return prefix + "-" + customerId + "-" + String.format("%02d", i);
    }

    private static BigDecimal insuredValueFor(PolicyCategory category, Random rnd) {
        BigDecimal value = switch (category) {
            case MOTOR -> BigDecimal.valueOf(20000L + rnd.nextInt(20000));     // 20k..40k
            case HEALTH -> BigDecimal.valueOf(300000L + rnd.nextInt(700000));  // 300k..1M
            // If your enum has only MOTOR/HEALTH, remove this default.
            // Keeping default prevents compilation error if more categories exist.
            default -> BigDecimal.valueOf(50000L + rnd.nextInt(100000));
        };
        return value.setScale(2, RoundingMode.HALF_UP);
    }


    private static BigDecimal premiumFor(PolicyCategory category, BigDecimal insuredValue, Random rnd) {
        // very rough: premium is small % of insured value, with variability
        BigDecimal baseRate =
                switch (category) {
                    case MOTOR -> new BigDecimal("0.030");  // 3%
                    case HEALTH -> new BigDecimal("0.020"); // 1%
                };
        BigDecimal noise = new BigDecimal(rnd.nextInt(200)).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP); // 0.00..1.99
        BigDecimal prem = insuredValue.multiply(baseRate).add(noise.multiply(new BigDecimal("100")));
        return prem.setScale(2, RoundingMode.HALF_UP);
    }

    private static String coverageFor(PolicyCategory category) {
        return switch (category) {
            case MOTOR -> "Comprehensive";
            case HEALTH -> "Hospitalization + OPD";
        };
    }

    private static String notesFor(PolicyCategory category) {
        return switch (category) {
            case MOTOR -> "Includes own damage, third‑party liability, and roadside assistance.";
            case HEALTH -> "Covers hospitalization, diagnostics and cashless network hospitals.";
        };
    }

    private static int riskScoreFor(String riskLevel, Random rnd) {
        return switch (riskLevel) {
            case "HIGH" -> 70 + rnd.nextInt(31);   // 70..100
            case "MEDIUM" -> 40 + rnd.nextInt(30); // 40..69
            default -> 10 + rnd.nextInt(30);       // 10..39
        };
    }

    private static double roundDouble(double v, int scale) {
        BigDecimal bd = BigDecimal.valueOf(v);
        return bd.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Tries to be compatible with either User#getUserId() or User#getId().
     * If your User entity only has one of these, simplify this method accordingly.
     */
    private static Long safeGetUserId(User u) {
        try {
            // common in your backend DTO naming
            return (Long) u.getClass().getMethod("getUserId").invoke(u);
        } catch (Exception ignore) {
            try {
                return (Long) u.getClass().getMethod("getId").invoke(u);
            } catch (Exception e) {
                throw new IllegalStateException("User entity has neither getUserId() nor getId()", e);
            }
        }
    }

    private record SeedCustomer(String name, String phone, String email, String riskLevel) {}
}