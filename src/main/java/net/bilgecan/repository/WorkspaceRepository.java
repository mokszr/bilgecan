package net.bilgecan.repository;

import net.bilgecan.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Page<Workspace> findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(String searchTerm, String searchTerm2, Pageable pageable);

    Workspace findByNameIgnoreCaseOrSlugIgnoreCase(String name, String slug);
}