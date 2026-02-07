package com.retainsure.service;

import com.retainsure.model.AuditLog;
import com.retainsure.repository.AuditLogRepository;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    public void logAction(String action, String entity, String entityId, String details) {
        String username = "system";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
        }
        logWithUsername(username, action, entity, entityId, details);
    }

    public void logWithUsername(String username, String action, String entity, String entityId, String details) {
        AuditLog log = new AuditLog(
                null,
                username,
                action,
                entity,
                entityId,
                details,
                LocalDateTime.now()
        );
        repo.save(log);
    }
}