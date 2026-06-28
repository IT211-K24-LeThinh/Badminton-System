package com.re.badmintonsystem.repository;

import com.re.badmintonsystem.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTableName(String tableName);
    List<AuditLog> findByTableNameAndRecordId(String tableName, Long recordId);
    List<AuditLog> findByAction(String action);
}
