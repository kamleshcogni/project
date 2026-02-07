package com.retainsure.service;

import com.retainsure.model.Customer;
import com.retainsure.model.User;
import com.retainsure.repository.CustomerRepository;
import com.retainsure.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository repo;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public CustomerService(CustomerRepository repo, UserRepository userRepository, AuditService auditService) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public List<Customer> listAll() { return repo.findAll(); }

    public Customer getById(Long id) { return repo.findById(id).orElseThrow(); }

    public Customer create(Customer c) {
        Customer saved = repo.save(c);
        auditService.logAction("CREATE", "Customer", String.valueOf(saved.getCustomerId()), "Created customer");
        return saved;
    }

    public Customer update(Long id, Customer c) {
        Customer existing = getById(id);
        existing.setName(c.getName());
        existing.setPhone(c.getPhone());
        existing.setEmail(c.getEmail());
        existing.setRiskLevel(c.getRiskLevel());
        // don't change userId in update
        Customer saved = repo.save(existing);
        auditService.logAction("UPDATE", "Customer", String.valueOf(saved.getCustomerId()), "Updated customer");
        return saved;
    }

    public void delete(Long id) {
        auditService.logAction("DELETE", "Customer", String.valueOf(id), "Deleted customer");
        repo.deleteById(id);
    }

    // ✅ NEW
    public Customer getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User u = userRepository.findByUsername(username).orElseThrow();
        return repo.findByUserId(u.getId()).orElseThrow();
    }
}