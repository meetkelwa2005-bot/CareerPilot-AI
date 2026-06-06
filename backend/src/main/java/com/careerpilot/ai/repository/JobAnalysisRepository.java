package com.careerpilot.ai.repository;

import com.careerpilot.ai.entity.JobAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, Long> {
    List<JobAnalysis> findByResumeId(Long resumeId);
}
