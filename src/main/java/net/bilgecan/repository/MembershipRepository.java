package net.bilgecan.repository;

import net.bilgecan.entity.Membership;
import net.bilgecan.entity.WorkspaceRole;
import net.bilgecan.entity.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Page<Membership> findByWorkspaceId(Long workspaceId, Pageable pageable);

    Page<Membership> findByUserUsernameContainingIgnoreCaseAndWorkspaceId(String searchTerm, Long workspaceId, Pageable pageable);

    Optional<Membership> findByWorkspaceIdAndUserId(Long workspaceId, Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Membership m set m.role = :role where m.id = :id")
    void updateRoleById(@Param("id") Long id, @Param("role") WorkspaceRole selected);

    List<Membership> findAllByUser(User currentUser);

    Membership findByUserAndWorkspaceId(User currentUser, Long workspaceId);

}