package com.agentteam.studentapproval.repository;

import com.agentteam.studentapproval.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, String> {

    @Query("SELECT a FROM Approval a JOIN FETCH a.application WHERE a.application.id = :applicationId")
    List<Approval> findByApplicationId(@Param("applicationId") String applicationId);

    Optional<Approval> findByApplicationIdAndApproverId(String applicationId, String approverId);

    boolean existsByApplicationIdAndApproverId(String applicationId, String approverId);

    @Query("SELECT a FROM Approval a JOIN FETCH a.application WHERE a.application.id = :applicationId AND a.action = 'PENDING'")
    List<Approval> findPendingApprovalsByApplication(@Param("applicationId") String applicationId);

    @Query("SELECT a FROM Approval a JOIN FETCH a.application LEFT JOIN FETCH a.approver WHERE a.approver.id = :approverId AND a.action = 'PENDING'")
    List<Approval> findPendingApprovalsByApprover(@Param("approverId") String approverId);
}
