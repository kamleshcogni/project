package com.retainsure.repository;

import com.retainsure.model.RenewalPrediction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RenewalPredictionRepository extends JpaRepository<RenewalPrediction, Long> {
    List<RenewalPrediction> findByCustomerId(Long customerId);
    List<RenewalPrediction> findByPolicyId(Long policyId);
    List<RenewalPrediction> findByRiskScoreGreaterThanEqual(int riskScore);
}