package com.retainsure.repository;
import com.retainsure.model.Reminder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByCustomerId(Long customerId);
    List<Reminder> findByPolicyId(Long policyId);
}
