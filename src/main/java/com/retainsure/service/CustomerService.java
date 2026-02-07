package com.retainsure.service;

import com.retainsure.model.Customer;
import com.retainsure.repository.CustomerRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository repo;
    private final AuditService auditService;

    public CustomerService(CustomerRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    public List<Customer> listAll() {
        return repo.findAll();
    }

    public Customer getById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public Customer create(Customer c) {
        Customer saved = repo.save(c);
        auditService.logAction("CREATE", "Customer", String.valueOf(saved.getCustomerId()), "Created customer");
        return saved;
    }

    public Customer update(Long id, Customer c) {
        Customer existing = getById(id);
        existing.setName(c.getName());
        existing.setContactInfo(c.getContactInfo());
        existing.setEmail(c.getEmail());
        existing.setRiskCategory(c.getRiskCategory());
        Customer saved = repo.save(existing);
        auditService.logAction("UPDATE", "Customer", String.valueOf(saved.getCustomerId()), "Updated customer");
        return saved;
    }

    public void delete(Long id) {
        auditService.logAction("DELETE", "Customer", String.valueOf(id), "Deleted customer");
        repo.deleteById(id);
    }
}