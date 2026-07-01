package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.entity.AuditLog;
import com.re.badmintonsystem.entity.User;
import com.re.badmintonsystem.entity.enums.LogStatus;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String entityType, Long entityId,
                    String status, String message, String ipAddress, String userAgent) {
        AuditLog auditLog = new AuditLog();

        if (userId != null) {
            User user = new User();
            user.setId(userId);
            auditLog.setUser(user);
        }

        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setStatus(LogStatus.valueOf(status));
        auditLog.setMessage(message);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);

        auditLogRepository.save(auditLog);
        log.debug("Audit log saved: entityType={}, entityId={}, action={}", entityType, entityId, action);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLog> findAll(String entityType, String action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditLog> logPage;
        if (entityType != null && !entityType.isBlank() && action != null && !action.isBlank()) {
            logPage = auditLogRepository.findByEntityTypeAndAction(entityType, action, pageable);
        } else if (entityType != null && !entityType.isBlank()) {
            logPage = auditLogRepository.findByEntityType(entityType, pageable);
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
