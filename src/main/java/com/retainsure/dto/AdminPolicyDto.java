package com.retainsure.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record AdminPolicyDto(
        @JsonProperty("policy_id") Long policyId,
        @JsonProperty("cust_id") Long custId,
        @JsonProperty("category") String category,
        @JsonProperty("policy_name") String policyName,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("start_date") String startDate,
        @JsonProperty("end_date") String endDate,
        @JsonProperty("renewal_date") String renewalDate,
        @JsonProperty("status") String status,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("modified_at") String modifiedAt
) {}