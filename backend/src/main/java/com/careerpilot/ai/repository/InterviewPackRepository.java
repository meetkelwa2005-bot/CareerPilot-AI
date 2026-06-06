package com.careerpilot.ai.repository;

import com.careerpilot.ai.entity.InterviewPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InterviewPackRepository extends JpaRepository<InterviewPack, Long> {
    Optional<InterviewPack> findByJobAnalysisId(Long jobAnalysisId);
}
