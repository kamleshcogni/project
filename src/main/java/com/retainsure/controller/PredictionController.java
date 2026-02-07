package com.retainsure.controller;

import com.retainsure.model.RenewalPrediction;
import com.retainsure.service.PredictionService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService service;

    public PredictionController(PredictionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RenewalPrediction> list() { return service.listAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RenewalPrediction get(@PathVariable Long id) { return service.getById(id); }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RenewalPrediction> byCustomer(@PathVariable Long customerId) {
        return service.listByCustomer(customerId);
    }

    @GetMapping("/policy/{policyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RenewalPrediction> byPolicy(@PathVariable Long policyId) {
        return service.listByPolicy(policyId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RenewalPrediction create(@RequestBody RenewalPrediction p) { return service.create(p); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RenewalPrediction update(@PathVariable Long id, @RequestBody RenewalPrediction p) {
        return service.update(id, p);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { service.delete(id); }
}