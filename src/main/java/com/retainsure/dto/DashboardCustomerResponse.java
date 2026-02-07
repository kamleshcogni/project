package com.retainsure.dto.customer;

import java.util.List;

public record DashboardCustomerResponse(
        NextRenewal nextRenewal,
        List<PolicyCard> policies,
        List<OfferCard> offers
) {
    public record NextRenewal(
            String product,
            String policyNumber,
            String renewalDate,
            String status
    ) {}

    public record PolicyCard(
            String name,
            String policyNumber,
            String status,
            String renewalDate,
            String expiredOn,
            double premium,
            String coverage,
            double insuredValue,
            String notes
    ) {}

    public record OfferCard(
            String title,
            String chip,
            String description,
            String conditions
    ) {}
}