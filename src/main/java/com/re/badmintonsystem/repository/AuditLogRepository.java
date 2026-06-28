package com.re.badmintonsystem.repository;

import com.re.badmintonsystem.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTableName(String tableName);
    Page<AuditLog> findByTableName(String tableName, Pageable pageable);
    List<AuditLog> findByTableNameAndRecordId(String tableName, Long recordId);
    List<AuditLog> findByAction(String action);
    Page<AuditLog> findByAction(String action, Pageable pageable);
    Page<AuditLog> findByTableNameAndAction(String tableName, String action, Pageable pageable);
}
