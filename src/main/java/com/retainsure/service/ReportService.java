package com.retainsure.service;

import com.retainsure.model.RetentionReport;
import com.retainsure.repository.RetentionReportRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final RetentionReportRepository repo;
    private final AuditService auditService;

    public ReportService(RetentionReportRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    public List<RetentionReport> listAll() { return repo.findAll(); }

    public RetentionReport getById(Long id) { return repo.findById(id).orElseThrow(); }

    public RetentionReport create(RetentionReport r) {
        RetentionReport saved = repo.save(r);
        auditService.logAction("CREATE", "RetentionReport", String.valueOf(saved.getReportId()), "Created report");
        return saved;
    }

    public RetentionReport update(Long id, RetentionReport r) {
        RetentionReport existing = getById(id);
        existing.setRenewalRate(r.getRenewalRate());
        existing.setChurnRate(r.getChurnRate());
        existing.setCampaignEffectiveness(r.getCampaignEffectiveness());
        existing.setGeneratedDate(r.getGeneratedDate());
        RetentionReport saved = repo.save(existing);
        auditService.logAction("UPDATE", "RetentionReport", String.valueOf(saved.getReportId()), "Updated report");
        return saved;
    }

    public void delete(Long id) {
        auditService.logAction("DELETE", "RetentionReport", String.valueOf(id), "Deleted report");
        repo.deleteById(id);
    }
}