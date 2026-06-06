package com.careerpilot.ai.repository;

import com.careerpilot.ai.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByJobAnalysisIdOrderByCreatedAtAsc(Long jobAnalysisId);
}
