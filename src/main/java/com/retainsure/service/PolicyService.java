package com.retainsure.service;

import com.retainsure.model.Policy;
import com.retainsure.repository.PolicyRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    private final PolicyRepository repo;
    private final AuditService auditService;

    public PolicyService(PolicyRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    public List<Policy> listAll() { return repo.findAll(); }

    public Policy getById(Long id) { return repo.findById(id).orElseThrow(); }

    public List<Policy> listByCustomer(Long customerId) { return repo.findByCustomerId(customerId); }

    public Policy create(Policy p) {
        Policy saved = repo.save(p);
        auditService.logAction("CREATE", "Policy", String.valueOf(saved.getPolicyId()), "Created policy");
        return saved;
    }

    public Policy update(Long id, Policy p) {
        Policy existing = getById(id);
        existing.setCustomerId(p.getCustomerId());
        existing.setPolicyType(p.getPolicyType());
        existing.setStartDate(p.getStartDate());
        existing.setEndDate(p.getEndDate());
        existing.setStatus(p.getStatus());
        Policy saved = repo.save(existing);
        auditService.logAction("UPDATE", "Policy", String.valueOf(saved.getPolicyId()), "Updated policy");
        return saved;
    }

    public void delete(Long id) {
        auditService.logAction("DELETE", "Policy", String.valueOf(id), "Deleted policy");
        repo.deleteById(id);
    }
}