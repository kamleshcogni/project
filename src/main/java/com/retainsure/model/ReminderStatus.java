package com.retainsure.model;

public enum ReminderStatus {
    SENT,
    RESPONDED,

    // Added for admin compatibility
    FAILED,
    SCHEDULED
}