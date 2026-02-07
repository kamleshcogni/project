package com.retainsure.controller;
import com.retainsure.model.RetentionReport;
import com.retainsure.service.ReportService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RetentionReport> list() { return service.listAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RetentionReport get(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RetentionReport create(@RequestBody RetentionReport r) { return service.create(r); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RetentionReport update(@PathVariable Long id, @RequestBody RetentionReport r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { service.delete(id); }
}