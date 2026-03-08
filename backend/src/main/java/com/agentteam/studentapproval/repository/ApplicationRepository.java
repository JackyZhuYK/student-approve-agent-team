package com.agentteam.studentapproval.repository;

import com.agentteam.studentapproval.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {

    Page<Application> findByApplicantId(String applicantId, Pageable pageable);

    Page<Application> findByStatus(Application.ApplicationStatus status, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.applicant.id = :applicantId ORDER BY a.submittedAt DESC")
    Page<Application> findUserApplications(@Param("applicantId") String applicantId, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.status = :status ORDER BY a.submittedAt ASC")
    Page<Application> findPendingApplications(Application.ApplicationStatus status, Pageable pageable);
}
