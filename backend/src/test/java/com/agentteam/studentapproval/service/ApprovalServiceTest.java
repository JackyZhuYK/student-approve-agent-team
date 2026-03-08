package com.agentteam.studentapproval.service;

import com.agentteam.studentapproval.dto.ApplicationRequest;
import com.agentteam.studentapproval.dto.ApprovalRequest;
import com.agentteam.studentapproval.entity.Application;
import com.agentteam.studentapproval.entity.Approval;
import com.agentteam.studentapproval.entity.User;
import com.agentteam.studentapproval.repository.ApplicationRepository;
import com.agentteam.studentapproval.repository.ApprovalRepository;
import com.agentteam.studentapproval.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalRepository approvalRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApprovalService approvalService;

    private User approver;
    private Application application;
    private Approval approval;

    @BeforeEach
    void setUp() {
        approver = User.builder()
            .id(UUID.randomUUID())
            .username("approver")
            .displayName("Test Approver")
            .role(User.Role.APPROVER)
            .build();

        application = Application.builder()
            .id(UUID.randomUUID())
            .title("Test Application")
            .description("Test Description")
            .type("LEAVE")
            .status(Application.ApplicationStatus.PENDING)
            .priority(Application.Priority.NORMAL)
            .build();

        approval = Approval.builder()
            .id(UUID.randomUUID())
            .application(application)
            .approver(approver)
            .action(Approval.ApprovalAction.PENDING)
            .build();
    }

    @Test
    void testGetPendingApprovals() {
        when(approvalRepository.findPendingApprovalsByApprover(approver.getId()))
            .thenReturn(List.of(approval));

        List<Approval> result = approvalService.getPendingApprovals(approver.getId());

        assertEquals(1, result.size());
        assertEquals(approval.getId(), result.get(0).getId());
        verify(approvalRepository).findPendingApprovalsByApprover(approver.getId());
    }

    @Test
    void testProcessApproval_Approve() {
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(approvalRepository.findByApplicationIdAndApproverId(application.getId(), approver.getId()))
            .thenReturn(Optional.of(approval));

        ApprovalRequest request = new ApprovalRequest("APPROVED", "Looks good");

        approvalService.processApproval(application.getId(), approver.getId(), request);

        ArgumentCaptor<Approval> captor = ArgumentCaptor.forClass(Approval.class);
        verify(approvalRepository).save(captor.capture());

        Approval savedApproval = captor.getValue();
        assertEquals(Approval.ApprovalAction.APPROVED, savedApproval.getAction());
        assertEquals("Looks good", savedApproval.getComment());
    }

    @Test
    void testProcessApproval_Reject() {
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(approvalRepository.findByApplicationIdAndApproverId(application.getId(), approver.getId()))
            .thenReturn(Optional.of(approval));

        ApprovalRequest request = new ApprovalRequest("REJECTED", "Does not meet requirements");

        approvalService.processApproval(application.getId(), approver.getId(), request);

        ArgumentCaptor<Approval> captor = ArgumentCaptor.forClass(Approval.class);
        verify(approvalRepository).save(captor.capture());

        Approval savedApproval = captor.getValue();
        assertEquals(Approval.ApprovalAction.REJECTED, savedApproval.getAction());
        assertEquals("Does not meet requirements", savedApproval.getComment());

        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void testGetApprovalsByApplication() {
        when(approvalRepository.findByApplicationId(application.getId()))
            .thenReturn(List.of(approval));

        List<Approval> result = approvalService.getApprovalsByApplication(application.getId());

        assertEquals(1, result.size());
        verify(approvalRepository).findByApplicationId(application.getId());
    }
}
