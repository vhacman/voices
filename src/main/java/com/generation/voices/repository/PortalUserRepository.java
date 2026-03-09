package com.generation.voices.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.generation.voices.model.PortalUser;

public interface PortalUserRepository extends JpaRepository<PortalUser, Integer> {

    Optional<PortalUser> findByUsername(String username);

}
