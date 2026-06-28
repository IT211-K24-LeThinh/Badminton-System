package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.entity.AuditLog;
import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.repository.AuditLogRepository;
import com.re.badmintonsystem.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void log(String tableName, Long recordId, String action, String oldValue, String newValue, String detail) {
        AuditLog auditLog = new AuditLog();
        auditLog.setTableName(tableName);
        auditLog.setRecordId(recordId);
        auditLog.setAction(action);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        auditLog.setDetail(detail);

        auditLogRepository.save(auditLog);
        log.debug("Audit log saved: table={}, recordId={}, action={}", tableName, recordId, action);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLog> findAll(String tableName, String action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "changedAt"));

        Page<AuditLog> logPage;
        if (tableName != null && !tableName.isBlank() && action != null && !action.isBlank()) {
            logPage = auditLogRepository.findByTableNameAndAction(tableName, action, pageable);
        } else if (tableName != null && !tableName.isBlank()) {
            logPage = auditLogRepository.findByTableName(tableName, pageable);
        } else if (action != null && !action.isBlank()) {
            logPage = auditLogRepository.findByAction(action, pageable);
        } else {
            logPage = auditLogRepository.findAll(pageable);
        }

        List<AuditLog> content = logPage.getContent();

        return new PagedResponse<>(content, logPage.getNumber(), logPage.getSize(),
                logPage.getTotalElements(), logPage.getTotalPages(), logPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLog findById(Long id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
    }
}
