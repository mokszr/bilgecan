package net.bilgecan.repository;

import net.bilgecan.entity.AITaskTemplate;
import net.bilgecan.entity.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AITaskTemplateRepository extends JpaRepository<AITaskTemplate, Long> {
    AITaskTemplate findByOwnerAndNameIgnoreCaseAndWorkspaceIsNull(User currentUser, String name);

    Page<AITaskTemplate> findByOwnerAndWorkspaceIsNull(User authenticatedUser, Pageable pageable);

    Page<AITaskTemplate> findByNameContainingIgnoreCaseAndOwnerAndWorkspaceIsNull(String searchTerm, User authenticatedUser, Pageable pageable);

    long deleteByIdAndOwnerAndWorkspaceIsNull(Long id, User currentUser);

    AITaskTemplate findByIdAndOwnerAndWorkspaceIsNull(Long aiTaskId, User currentUser);

    AITaskTemplate findByWorkspaceIdAndNameIgnoreCase(Long workspaceId, String name);

    Page<AITaskTemplate> findByWorkspaceId(Long workspaceId, Pageable pageable);

    Page<AITaskTemplate> findByNameContainingIgnoreCaseAndWorkspaceId(String searchTerm, Long workspaceId, Pageable pageable);

    long deleteByIdAndWorkspaceId(Long id, Long workspaceId);

    AITaskTemplate findByIdAndWorkspaceId(Long id, Long workspaceId);

}

