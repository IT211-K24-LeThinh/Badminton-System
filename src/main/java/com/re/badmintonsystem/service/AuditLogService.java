package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.entity.AuditLog;

public interface AuditLogService {

    void log(Long userId, String action, String entityType, Long entityId,
             String status, String message, String ipAddress, String userAgent);

    PagedResponse<AuditLog> findAll(String entityType, String action, int page, int size);

    AuditLog findById(Long id);
}
