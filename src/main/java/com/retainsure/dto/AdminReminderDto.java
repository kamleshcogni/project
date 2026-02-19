package com.retainsure.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminReminderDto(
        @JsonProperty("cust_id") Long custId,
        @JsonProperty("policy_id") Long policyId,
        @JsonProperty("category") String category,
        @JsonProperty("date_sent") String dateSent,
        @JsonProperty("mode") String mode,
        @JsonProperty("risk_score") Integer riskScore,
        @JsonProperty("trigger") String trigger,
        @JsonProperty("status") String status
) {}