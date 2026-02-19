package com.retainsure.service;

import com.retainsure.model.Campaign;
import com.retainsure.repository.CampaignRepository;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CampaignService {

    private final CampaignRepository repo;
    private final AuditService auditService;

    public CampaignService(CampaignRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    public List<Campaign> listAll() { return repo.findAll(); }

    public Campaign getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Campaign not found: " + id));
    }
    public Campaign create(Campaign c) {
        Campaign saved = repo.save(c);
        auditService.logAction("CREATE", "Campaign", String.valueOf(saved.getCampaignId()), "Created campaign");
        return saved;
    }

    public Campaign update(Long id, Campaign c) {
        Campaign existing = getById(id);
        existing.setCampaignName(c.getCampaignName());
        existing.setTarget(c.getTarget());
        existing.setStartDate(c.getStartDate());
        existing.setEndDate(c.getEndDate());
        existing.setStatus(c.getStatus());
        existing.setDiscountPercent(c.getDiscountPercent());
        existing.setCampaignCode(c.getCampaignCode());
        Campaign saved = repo.save(existing);
        auditService.logAction("UPDATE", "Campaign", String.valueOf(saved.getCampaignId()), "Updated campaign");
        return saved;
    }

    public void delete(Long id) {
        auditService.logAction("DELETE", "Campaign", String.valueOf(id), "Deleted campaign");
        repo.deleteById(id);
    }
}
