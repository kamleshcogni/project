package com.retainsure.service;

import com.retainsure.model.Reminder;
import com.retainsure.repository.ReminderRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {

    private final ReminderRepository repo;
    private final AuditService auditService;

    public ReminderService(ReminderRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    public List<Reminder> listAll() { return repo.findAll(); }

    public Reminder getById(Long id) { return repo.findById(id).orElseThrow(); }

    public List<Reminder> listByCustomer(Long customerId) { return repo.findByCustomerId(customerId); }

    public List<Reminder> listByPolicy(Long policyId) { return repo.findByPolicyId(policyId); }

    public Reminder create(Reminder r) {
        Reminder saved = repo.save(r);
        auditService.logAction("CREATE", "Reminder", String.valueOf(saved.getReminderId()), "Created reminder");
        return saved;
    }

    public Reminder update(Long id, Reminder r) {
        Reminder existing = getById(id);
        existing.setCustomerId(r.getCustomerId());
        existing.setPolicyId(r.getPolicyId());
        existing.setMessage(r.getMessage());
        existing.setSentDate(r.getSentDate());
        existing.setStatus(r.getStatus());
        Reminder saved = repo.save(existing);
        auditService.logAction("UPDATE", "Reminder", String.valueOf(saved.getReminderId()), "Updated reminder");
        return saved;
    }

    public void delete(Long id) {
        auditService.logAction("DELETE", "Reminder", String.valueOf(id), "Deleted reminder");
        repo.deleteById(id);
    }
}