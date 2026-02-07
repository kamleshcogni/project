package com.retainsure.service;

import com.retainsure.dto.customer.DashboardCustomerResponse;
import com.retainsure.model.Customer;
import com.retainsure.model.Policy;
import com.retainsure.model.Reminder;
import com.retainsure.model.User;
import com.retainsure.repository.CustomerRepository;
import com.retainsure.repository.PolicyRepository;
import com.retainsure.repository.ReminderRepository;
import com.retainsure.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
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

    public CustomerDashboardService(UserRepository userRepository,
                                    CustomerRepository customerRepository,
                                    PolicyRepository policyRepository,
                                    ReminderRepository reminderRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.policyRepository = policyRepository;
        this.reminderRepository = reminderRepository;
    }

    public DashboardCustomerResponse getMyDashboard() {
        Customer customer = resolveLoggedInCustomer();

        List<Policy> policies = policyRepository.findByCustomerId(customer.getCustomerId());
        List<Reminder> reminders = reminderRepository.findByCustomerId(customer.getCustomerId());

        // Map policies -> UI shape
        List<DashboardCustomerResponse.PolicyCard> uiPolicies = policies.stream()
                .map(p -> new DashboardCustomerResponse.PolicyCard(
                        policyName(p.getPolicyType()),
                        "POL-" + p.getPolicyId(),
                        String.valueOf(p.getStatus()),
                        isActive(p) ? p.getEndDate() : null,
                        isExpired(p) ? p.getEndDate() : null,
                        0.0,
                        "-",
                        0.0,
                        "—"
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
        List<DashboardCustomerResponse.OfferCard> offers = reminders.stream()
                .limit(3)
                .map(r -> new DashboardCustomerResponse.OfferCard(
                        "Retention Offer",
                        "Limited",
                        r.getMessage(),
                        "Sent on " + r.getSentDate()
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