package com.retainsure.service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.retainsure.model.Campaign;
import com.retainsure.dto.customer.DashboardCustomerResponse;
import com.retainsure.model.Customer;
import com.retainsure.model.Policy;
import com.retainsure.model.Reminder;
import com.retainsure.model.User;
import com.retainsure.repository.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CustomerDashboardService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PolicyRepository policyRepository;
    private final ReminderRepository reminderRepository;
    private final CampaignRepository campaignRepository;

    public CustomerDashboardService(UserRepository userRepository,
                                    CustomerRepository customerRepository,
                                    PolicyRepository policyRepository,
                                    ReminderRepository reminderRepository, CampaignRepository campaignRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.policyRepository = policyRepository;
        this.reminderRepository = reminderRepository;
        this.campaignRepository = campaignRepository;
    }

    private boolean isWithinCampaignWindow(Campaign c) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd
        LocalDate today = LocalDate.now();

        LocalDate start = c.getStartDate() != null ? LocalDate.parse(c.getStartDate(), fmt) : null;
        LocalDate end = c.getEndDate() != null ? LocalDate.parse(c.getEndDate(), fmt) : null;

        if (start != null && today.isBefore(start)) return false;
        if (end != null && today.isAfter(end)) return false;
        return true;
    }

    private String safeLower(String v) {
        return v == null ? null : v.trim().toLowerCase();
    }

    public DashboardCustomerResponse getMyDashboard() {
        Customer customer = resolveLoggedInCustomer();

        List<Policy> policies = policyRepository.findByCustomerId(customer.getCustomerId());
        List<Reminder> reminders = reminderRepository.findByCustomerId(customer.getCustomerId());

        // Map policies -> UI shape
        List<DashboardCustomerResponse.PolicyCard> uiPolicies = policies.stream()
                .map(p -> new DashboardCustomerResponse.PolicyCard(
                        policyName(p.getPolicyType()),
                        p.getPolicyNumber() != null ? p.getPolicyNumber() : "POL-" + p.getPolicyId(),
                        String.valueOf(p.getStatus()),
                        isActive(p) ? p.getEndDate() : null,
                        isExpired(p) ? p.getEndDate() : null,
                        p.getPremium() != null ? p.getPremium().doubleValue() : 0.0,
                        p.getCoverage() != null ? p.getCoverage() : "-",
                        p.getInsuredValue() != null
                                ? p.getInsuredValue().doubleValue()
                                : (p.getAmount() != null ? p.getAmount().doubleValue() : 0.0),
                        p.getNotes() != null ? p.getNotes() : "—"
                ))
                .toList();
        // Next renewal: earliest ACTIVE endDate
        Optional<Policy> next = policies.stream()
                .filter(this::isActive)
                .min(Comparator.comparing(Policy::getEndDate, Comparator.nullsLast(String::compareTo)));

        DashboardCustomerResponse.NextRenewal nextRenewal = next
                .map(p -> new DashboardCustomerResponse.NextRenewal(
                        p.getPolicyType(),
                        "POL-" + p.getPolicyId(),
                        p.getEndDate(),
                        "DUE"
                ))
                .orElseGet(() -> new DashboardCustomerResponse.NextRenewal("-", "-", "", "N/A"));

        // Offers: derive from reminders (as your earlier Angular mapping did)
        List<DashboardCustomerResponse.OfferCard> offers = campaignRepository.findAll().stream()
                .filter(c -> c.getStatus() != null && c.getStatus().name().equalsIgnoreCase("ACTIVE"))
                .filter(c -> Objects.equals(
                        safeLower(c.getTarget()),
                        safeLower(customer.getRiskLevel())
                ))
                .filter(this::isWithinCampaignWindow)
                .sorted(Comparator.comparing(Campaign::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(c -> new DashboardCustomerResponse.OfferCard(
                        c.getCampaignName(),
                        c.getDiscountPercent() != null ? c.getDiscountPercent() + "% OFF" : "Offer",
                        "Target: " + c.getTarget(),
                        "Valid: " + c.getStartDate() + " to " + c.getEndDate()
                ))
                .toList();
        return new DashboardCustomerResponse(nextRenewal, uiPolicies, offers);
    }

    private Customer resolveLoggedInCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found for username: " + username));

        // This requires your Customer table has userId (as discussed earlier)
        return customerRepository.findByUserId(u.getId())
                .orElseThrow(() -> new IllegalStateException("Customer not linked to userId: " + u.getId()));
    }

    private boolean isActive(Policy p) {
        return p.getStatus() != null && p.getStatus().toString().equalsIgnoreCase("ACTIVE");
    }

    private boolean isExpired(Policy p) {
        return p.getStatus() != null && p.getStatus().toString().equalsIgnoreCase("EXPIRED");
    }

    private String policyName(String type) {
        if (type == null) return "-";
        return switch (type.toUpperCase()) {
            case "HEALTH" -> "Health Insurance";
            case "MOTOR" -> "Motor Insurance";
            default -> type;
        };
    }
}