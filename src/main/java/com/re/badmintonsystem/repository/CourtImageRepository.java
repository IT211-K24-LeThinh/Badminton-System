package com.re.badmintonsystem.repository;

import com.re.badmintonsystem.entity.CourtImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourtImageRepository extends JpaRepository<CourtImage, Long> {
    List<CourtImage> findByCourtIdOrderByDisplayOrderAsc(Long courtId);
    List<CourtImage> findByCourtIdAndIsPrimaryTrue(Long courtId);
}
