package com.agentteam.studentapproval.service;

import com.agentteam.studentapproval.dto.ApplicationRequest;
import com.agentteam.studentapproval.dto.ApplicationResponse;
import com.agentteam.studentapproval.entity.Application;
import com.agentteam.studentapproval.entity.Approval;
import com.agentteam.studentapproval.entity.User;
import com.agentteam.studentapproval.repository.ApplicationRepository;
import com.agentteam.studentapproval.repository.ApprovalRepository;
import com.agentteam.studentapproval.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ApprovalRepository approvalRepository;

    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request, String userId) {
        User applicant = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Application application = Application.builder()
            .applicant(applicant)
            .title(request.getTitle())
            .description(request.getDescription())
            .type(request.getType())
            .priority(Application.Priority.valueOf(request.getPriority()))
            .attachmentUrls(request.getAttachmentUrls())
            .status(Application.ApplicationStatus.PENDING)
            .build();

        Application saved = applicationRepository.save(application);

        // Create pending approvals for all approvers
        List<User> approvers = userRepository.findAll().stream()
            .filter(u -> u.getRole() == User.Role.APPROVER ||
                        u.getRole() == User.Role.ADMIN)
            .collect(Collectors.toList());

        for (User approver : approvers) {
            Approval approval = Approval.builder()
                .application(saved)
                .approver(approver)
                .action(Approval.ApprovalAction.PENDING)
                .build();
            approvalRepository.save(approval);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getById(String id) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getUserApplications(String userId, Pageable pageable) {
        return applicationRepository.findUserApplications(userId, pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAllApplications(Pageable pageable) {
        return applicationRepository.findAll(pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getPendingApplications(Pageable pageable) {
        return applicationRepository.findPendingApplications(Application.ApplicationStatus.PENDING, pageable)
            .map(this::toResponse);
    }

    @Transactional
    public ApplicationResponse withdrawApplication(String id, String userId) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getApplicant().getId().equals(userId)) {
            throw new RuntimeException("Only the applicant can withdraw");
        }

        application.setWithdrawn(true);
        application.setWithdrawnAt(LocalDateTime.now());
        application.setStatus(Application.ApplicationStatus.WITHDRAWN);

        return toResponse(applicationRepository.save(application));
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
