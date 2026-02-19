package com.retainsure.service;

import com.retainsure.model.RenewalPrediction;
import com.retainsure.repository.RenewalPredictionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PredictionService {

    private final RenewalPredictionRepository repo;
    private final AuditService auditService;

    public PredictionService(RenewalPredictionRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    public List<RenewalPrediction> listAll() { return repo.findAll(); }

    public RenewalPrediction getById(Long id) { return repo.findById(id).orElseThrow(); }

    public List<RenewalPrediction> listByCustomer(Long customerId) { return repo.findByCustomerId(customerId); }

    public List<RenewalPrediction> listByPolicy(Long policyId) { return repo.findByPolicyId(policyId); }

    public RenewalPrediction create(RenewalPrediction p) {
        RenewalPrediction saved = repo.save(p);
        auditService.logAction("CREATE", "RenewalPrediction", String.valueOf(saved.getPredictionId()), "Created prediction");
        return saved;
    }

    public RenewalPrediction update(Long id, RenewalPrediction p) {
        RenewalPrediction existing = getById(id);
        existing.setCustomerId(p.getCustomerId());
        existing.setPolicyId(p.getPolicyId());
        existing.setRenewalProbability(p.getRenewalProbability());
        existing.setRiskScore(p.getRiskScore());
        RenewalPrediction saved = repo.save(existing);
        auditService.logAction("UPDATE", "RenewalPrediction", String.valueOf(saved.getPredictionId()), "Updated prediction");
        return saved;
    }

    public void delete(Long id) {
        auditService.logAction("DELETE", "RenewalPrediction", String.valueOf(id), "Deleted prediction");
        repo.deleteById(id);
    }
}