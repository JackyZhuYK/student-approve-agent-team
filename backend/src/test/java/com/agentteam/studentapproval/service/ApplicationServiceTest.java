package com.agentteam.studentapproval.service;

import com.agentteam.studentapproval.dto.ApplicationRequest;
import com.agentteam.studentapproval.entity.Application;
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
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApprovalRepository approvalRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private User applicant;
    private ApplicationRequest request;

    @BeforeEach
    void setUp() {
        applicant = User.builder()
            .id(UUID.randomUUID())
            .username("student")
            .displayName("Test Student")
            .role(User.Role.STUDENT)
            .build();

        request = new ApplicationRequest(
            "Test Application",
            "Test Description",
            "LEAVE",
            "NORMAL",
            null
        );
    }

    @Test
    void testCreateApplication() {
        when(userRepository.findById(applicant.getId())).thenReturn(Optional.of(applicant));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArguments()[0]);

        var response = applicationService.createApplication(request, applicant.getId());

        assertNotNull(response);
        assertEquals("Test Application", response.getTitle());
        assertEquals("PENDING", response.getStatus());

        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void testGetById() {
        Application app = Application.builder()
            .id(UUID.randomUUID())
            .title("Test")
            .description("Desc")
            .type("LEAVE")
            .status(Application.ApplicationStatus.PENDING)
            .build();

        when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));

        var response = applicationService.getById(app.getId());

        assertNotNull(response);
        assertEquals("Test", response.getTitle());
    }

    @Test
    void testGetById_NotFound() {
        UUID notFoundId = UUID.randomUUID();
        when(applicationRepository.findById(notFoundId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            applicationService.getById(notFoundId);
        });
    }

    @Test
    void testWithdrawApplication() {
        Application app = Application.builder()
            .id(UUID.randomUUID())
            .applicant(applicant)
            .title("Test")
            .status(Application.ApplicationStatus.PENDING)
            .build();

        when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArguments()[0]);

        var response = applicationService.withdrawApplication(app.getId(), applicant.getId());

        assertNotNull(response);
        assertEquals("WITHDRAWN", response.getStatus());
        assertTrue(response.getWithdrawn());
    }

    @Test
    void testWithdrawApplication_Unauthorized() {
        Application app = Application.builder()
            .id(UUID.randomUUID())
            .applicant(applicant)
            .title("Test")
            .status(Application.ApplicationStatus.PENDING)
            .build();

        UUID otherUserId = UUID.randomUUID();
        when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));

        assertThrows(RuntimeException.class, () -> {
            applicationService.withdrawApplication(app.getId(), otherUserId);
        });
    }
}
