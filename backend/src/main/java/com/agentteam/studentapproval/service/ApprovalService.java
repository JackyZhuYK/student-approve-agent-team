package com.agentteam.studentapproval.service;

import com.agentteam.studentapproval.dto.ApprovalRequest;
import com.agentteam.studentapproval.dto.ApplicationResponse;
import com.agentteam.studentapproval.entity.Application;
import com.agentteam.studentapproval.entity.Approval;
import com.agentteam.studentapproval.entity.User;
import com.agentteam.studentapproval.repository.ApplicationRepository;
import com.agentteam.studentapproval.repository.ApprovalRepository;
import com.agentteam.studentapproval.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Approval> getPendingApprovals(String userId) {
        return approvalRepository.findPendingApprovalsByApprover(userId);
    }

    @Transactional(readOnly = true)
    public List<Approval> getApprovalsByApplication(String applicationId) {
        return approvalRepository.findByApplicationId(applicationId);
    }

    @Transactional
    public ApplicationResponse processApproval(String applicationId, String approverId, ApprovalRequest request) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        Approval approval = approvalRepository.findByApplicationIdAndApproverId(applicationId, approverId)
            .orElseGet(() -> {
                User approver = userRepository.findById(approverId)
                    .orElseThrow(() -> new RuntimeException("Approver not found"));

                Approval newApproval = Approval.builder()
                    .application(application)
                    .approver(approver)
                    .action(Approval.ApprovalAction.PENDING)
                    .build();
                return approvalRepository.save(newApproval);
            });

        // Update approval action
        approval.setAction(Approval.ApprovalAction.valueOf(request.getAction().toUpperCase()));
        approval.setComment(request.getComment());
        approvalRepository.save(approval);

        // Update application status based on all approvals
        updateApplicationStatus(application);

        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse agentApprove(String applicationId, String agentRole, String comment) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        // Find or create agent approval
        Approval approval = approvalRepository.findByApplicationIdAndApproverId(applicationId, null)
            .orElseGet(() -> {
                Approval newApproval = Approval.builder()
                    .application(application)
                    .action(Approval.ApprovalAction.PENDING)
                    .agentRole(agentRole)
                    .build();
                return approvalRepository.save(newApproval);
            });

        approval.setAction(Approval.ApprovalAction.APPROVED);
        approval.setComment(comment);
        approval.setAgentRole(agentRole);
        approvalRepository.save(approval);

        updateApplicationStatus(application);

        return toResponse(application);
    }

    private void updateApplicationStatus(Application application) {
        List<Approval> approvals = approvalRepository.findByApplicationId(application.getId());

        boolean allApproved = approvals.stream()
            .allMatch(a -> a.getAction() == Approval.ApprovalAction.APPROVED);

        boolean anyRejected = approvals.stream()
            .anyMatch(a -> a.getAction() == Approval.ApprovalAction.REJECTED);

        if (anyRejected) {
            application.setStatus(Application.ApplicationStatus.REJECTED);
            application.setCompletedAt(LocalDateTime.now());
        } else if (allApproved) {
            application.setStatus(Application.ApplicationStatus.APPROVED);
            application.setCompletedAt(LocalDateTime.now());
        } else {
            application.setStatus(Application.ApplicationStatus.IN_REVIEW);
        }

        applicationRepository.save(application);
    }

    private ApplicationResponse toResponse(Application app) {
        List<ApplicationResponse.ApprovalDTO> approvalDTOs = approvalRepository
            .findByApplicationId(app.getId())
            .stream()
            .map(a -> ApplicationResponse.ApprovalDTO.builder()
                .id(a.getId())
                .approverName(a.getApprover() != null ? a.getApprover().getDisplayName() : "System")
                .agentRole(a.getAgentRole())
                .action(a.getAction().name())
                .comment(a.getComment())
                .createdAt(a.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        return ApplicationResponse.builder()
            .id(app.getId())
            .title(app.getTitle())
            .description(app.getDescription())
            .type(app.getType())
            .status(app.getStatus().name())
            .priority(app.getPriority().name())
            .attachmentUrls(app.getAttachmentUrls())
            .applicant(ApplicationResponse.UserInfo.builder()
                .id(app.getApplicant().getId())
                .username(app.getApplicant().getUsername())
                .displayName(app.getApplicant().getDisplayName())
                .build())
            .submittedAt(app.getSubmittedAt())
            .updatedAt(app.getUpdatedAt())
            .completedAt(app.getCompletedAt())
            .approvals(approvalDTOs)
            .build();
    }
}
