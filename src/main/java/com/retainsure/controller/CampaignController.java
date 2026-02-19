package com.retainsure.controller;

import com.retainsure.model.Campaign;
import com.retainsure.service.CampaignService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService service;

    public CampaignController(CampaignService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Campaign> list() { return service.listAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Campaign get(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Campaign create(@RequestBody Campaign c) { return service.create(c); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Campaign update(@PathVariable("id") Long id, @RequestBody Campaign c) {
        return service.update(id, c);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
