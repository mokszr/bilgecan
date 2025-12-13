package net.bilgecan.repository;

import net.bilgecan.entity.FeedRagHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRagHistoryRepository extends JpaRepository<FeedRagHistory,Long> {
    Page<FeedRagHistory> findAllByOrderByDateDesc(Pageable pageable);
}
