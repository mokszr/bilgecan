package net.bilgecan.repository;

import net.bilgecan.entity.FileProcessingPipeline;
import net.bilgecan.entity.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileProcessingPipelineRepository extends JpaRepository<FileProcessingPipeline, Long> {
    FileProcessingPipeline findByOwnerAndName(User currentUser, String name);

    Page<FileProcessingPipeline> findByOwner(User authenticatedUser, Pageable pageable);

    Page<FileProcessingPipeline> findByNameContainingIgnoreCaseAndOwner(String searchTerm, User authenticatedUser, Pageable pageable);

    long deleteByIdAndOwner(Long id, User currentUser);

    FileProcessingPipeline findByIdAndOwner(Long aiTaskId, User currentUser);

}

