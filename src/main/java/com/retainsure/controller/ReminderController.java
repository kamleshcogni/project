package com.retainsure.controller;

import com.retainsure.dto.BulkReminderRequest;
import com.retainsure.model.Reminder;
import com.retainsure.service.ReminderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService service;

    public ReminderController(ReminderService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Reminder> list() { return service.listAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Reminder get(@PathVariable Long id) { return service.getById(id); }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public List<Reminder> byCustomer(@PathVariable Long customerId) {
        return service.listByCustomer(customerId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Reminder create(@RequestBody Reminder r) { return service.create(r); }

    /** ✅ NEW bulk endpoint */
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Reminder> bulk(@Valid @RequestBody BulkReminderRequest req) {
        return service.bulkCreate(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Reminder update(@PathVariable Long id, @RequestBody Reminder r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { service.delete(id); }
}