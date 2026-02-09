package com.agarg.securecollab.botservice.repository;

import com.agarg.securecollab.botservice.model.BotWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BotWorkflowRepository extends JpaRepository<BotWorkflow, Long> {
    List<BotWorkflow> findByTriggerTypeAndEnabledTrue(String triggerType);
    List<BotWorkflow> findByEnabledTrue();
}
