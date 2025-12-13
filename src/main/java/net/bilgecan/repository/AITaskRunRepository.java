package net.bilgecan.repository;

import net.bilgecan.entity.AITaskRun;
import net.bilgecan.entity.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AITaskRunRepository extends JpaRepository<AITaskRun, Long>, AITaskRunRepositoryCustom {
    Page<AITaskRun> findByOwnerOrderByCreatedAtDesc(User authenticatedUser, Pageable pageable);

    long deleteByIdAndOwner(Long id, User currentUser);

    @Query("""
        select distinct r
        from AITaskRun r
        where (r.owner = :user and r.workspace is null)
           or exists (
                select 1
                from Membership m
                where m.workspace = r.workspace
                  and m.user = :user
           )
        order by r.createdAt desc
        """)
    Page<AITaskRun> findVisibleForUser(@Param("user") User user, Pageable pageable);

    long deleteByIdAndWorkspaceId(Long id, Long workspaceId);
}
