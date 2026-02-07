package com.retainsure.controller;

import com.retainsure.model.Customer;
import com.retainsure.service.CustomerService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Customer> list() { return service.listAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public Customer get(@PathVariable Long id) { return service.getById(id); }

    // ✅ NEW: resolve customer record for logged-in user
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Customer me() { return service.getMe(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Customer create(@RequestBody Customer c) { return service.create(c); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Customer update(@PathVariable Long id, @RequestBody Customer c) {
        return service.update(id, c);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { service.delete(id); }
}