package com.retainsure.controller;

import com.retainsure.model.Policy;
import com.retainsure.service.PolicyService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService service;

    public PolicyController(PolicyService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Policy> list() { return service.listAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public Policy get(@PathVariable Long id) { return service.getById(id); }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public List<Policy> byCustomer(@PathVariable Long customerId) {
        return service.listByCustomer(customerId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Policy create(@RequestBody Policy p) { return service.create(p); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Policy update(@PathVariable Long id, @RequestBody Policy p) {
        return service.update(id, p);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { service.delete(id); }
}