package com.retainsure.service;

import com.retainsure.dto.BulkReminderRequest;
import com.retainsure.model.Reminder;
import com.retainsure.model.Reminder.ReminderStatus;
import com.retainsure.model.RenewalPrediction;
import com.retainsure.repository.ReminderRepository;
import com.retainsure.repository.RenewalPredictionRepository;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {

    private final ReminderRepository repo;
    private final RenewalPredictionRepository predictionRepo;
    private final AuditService auditService;

    public ReminderService(ReminderRepository repo,
                           RenewalPredictionRepository predictionRepo,
                           AuditService auditService) {
        this.repo = repo;
        this.predictionRepo = predictionRepo;
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

    /** ✅ Bulk create reminders for all customers with riskScore >= threshold */
    public List<Reminder> bulkCreate(BulkReminderRequest req) {
        List<RenewalPrediction> risky = predictionRepo.findByRiskScoreGreaterThanEqual(req.riskThreshold());

        // De-dup by (customerId, policyId) so we don't create duplicates if predictions repeat
        Set<String> seen = new HashSet<>();
        List<Reminder> toSave = new ArrayList<>();

        for (RenewalPrediction p : risky) {
            String key = p.getCustomerId() + ":" + p.getPolicyId();
            if (!seen.add(key)) continue;

            Reminder r = new Reminder();
            r.setCustomerId(p.getCustomerId());
            r.setPolicyId(p.getPolicyId());
            r.setMessage(req.triggerMsg());
            r.setSentDate(req.dateSent());
            r.setStatus(ReminderStatus.SENT); // backend only supports SENT/RESPONDED currently

            toSave.add(r);
        }

        List<Reminder> saved = repo.saveAll(toSave);
        auditService.logAction(
                "BULK_CREATE",
                "Reminder",
                "",
                "Bulk created " + saved.size() + " reminders for riskThreshold=" + req.riskThreshold()
        );
        return saved;
    }
}