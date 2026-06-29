package com.re.badmintonsystem.repository;

import com.re.badmintonsystem.entity.Court;
import com.re.badmintonsystem.entity.Court.CourtStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    Optional<Court> findByCourtCode(String courtCode);
    boolean existsByCourtCode(String courtCode);
    Page<Court> findByStatus(CourtStatus status, Pageable pageable);
    Page<Court> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
