package com.retainsure.dto;

public record AuthResponse(String token, String role, Long userId) {}
