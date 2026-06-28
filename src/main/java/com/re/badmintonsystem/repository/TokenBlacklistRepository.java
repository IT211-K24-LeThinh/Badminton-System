package com.re.badmintonsystem.repository;

import com.re.badmintonsystem.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    Optional<TokenBlacklist> findByTokenHash(String tokenHash);
    boolean existsByTokenHash(String tokenHash);
}
