package net.bilgecan.repository;

import net.bilgecan.entity.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String searchTerm, String searchTerm2, Pageable pageable);

    long countByEnabled(boolean enabled);
}