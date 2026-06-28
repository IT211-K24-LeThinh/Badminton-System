package com.re.badmintonsystem.repository;

import com.re.badmintonsystem.entity.CourtComplex;
import com.re.badmintonsystem.entity.CourtComplex.ComplexStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourtComplexRepository extends JpaRepository<CourtComplex, Long> {
    Optional<CourtComplex> findByName(String name);
    boolean existsByName(String name);
    List<CourtComplex> findByManagerId(Long managerId);
    Page<CourtComplex> findByStatus(ComplexStatus status, Pageable pageable);
    Page<CourtComplex> findByStatusAndNameContainingIgnoreCase(
            ComplexStatus status, String name, Pageable pageable);
}
