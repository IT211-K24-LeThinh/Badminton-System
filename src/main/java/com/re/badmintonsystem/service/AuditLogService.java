package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.entity.AuditLog;

public interface AuditLogService {

    void log(String tableName, Long recordId, String action, String oldValue, String newValue, String detail);

    PagedResponse<AuditLog> findAll(String tableName, String action, int page, int size);

    AuditLog findById(Long id);
}
