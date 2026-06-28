package com.re.badmintonsystem.repository;

import com.re.badmintonsystem.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByComplexId(Long complexId);
    Optional<Court> findByCourtCode(String courtCode);
    boolean existsByCourtCode(String courtCode);
}
