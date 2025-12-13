package net.bilgecan.repository;

import net.bilgecan.entity.Prompt;
import net.bilgecan.entity.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    Prompt findByOwnerAndNameIgnoreCaseAndWorkspaceIsNull(User currentUser, String name);

    Page<Prompt> findByOwnerAndWorkspaceIsNull(User authenticatedUser, Pageable pageable);

    Page<Prompt> findByNameContainingIgnoreCaseAndOwnerAndWorkspaceIsNull(String searchTerm, User authenticatedUser, Pageable pageable);

    long deleteByIdAndOwnerAndWorkspaceIsNull(Long id, User currentUser);

    Prompt findByIdAndOwnerAndWorkspaceIsNull(Long promptId, User currentUser);

    Prompt findByWorkspaceIdAndNameIgnoreCase(Long workspaceId, String name);

    long deleteByIdAndWorkspaceId(Long id, Long workspaceId);

    Page<Prompt> findByWorkspaceId(Long workspaceId, Pageable pageable);

    Page<Prompt> findByNameContainingIgnoreCaseAndWorkspaceId(String searchTerm, Long workspaceId, Pageable pageable);

    Prompt findByIdAndWorkspaceId(Long promptId, Long wsId);

}

