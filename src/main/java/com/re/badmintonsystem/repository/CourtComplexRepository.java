package com.re.badmintonsystem.repository;

import com.re.badmintonsystem.entity.CourtComplex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourtComplexRepository extends JpaRepository<CourtComplex, Long> {
    Optional<CourtComplex> findByName(String name);
    boolean existsByName(String name);
}
