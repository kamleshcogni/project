package com.retainsure.controller;

import com.retainsure.dto.customer.DashboardCustomerResponse;
import com.retainsure.service.CustomerDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerDashboardController {

    private final CustomerDashboardService service;

    public CustomerDashboardController(CustomerDashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CUSTOMER')")
    public DashboardCustomerResponse dashboard() {
        return service.getMyDashboard();
    }
}