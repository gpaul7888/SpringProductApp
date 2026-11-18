package com.company.project.auth.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, UUID> {

    /** Emails are stored lower-cased, so callers must normalise before querying. */
    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
