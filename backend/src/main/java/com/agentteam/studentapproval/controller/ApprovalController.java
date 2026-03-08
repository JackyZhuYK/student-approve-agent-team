package com.agentteam.studentapproval.controller;

import com.agentteam.studentapproval.dto.ApprovalRequest;
import com.agentteam.studentapproval.dto.ApplicationResponse;
import com.agentteam.studentapproval.entity.Approval;
import com.agentteam.studentapproval.entity.User;
import com.agentteam.studentapproval.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/pending")
    public ResponseEntity<List<Approval>> getPendingApprovals(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(approvalService.getPendingApprovals(user.getId()));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<Approval>> getApprovalsByApplication(@PathVariable String applicationId) {
        return ResponseEntity.ok(approvalService.getApprovalsByApplication(applicationId));
    }

    @PostMapping("/application/{applicationId}")
    public ResponseEntity<ApplicationResponse> processApproval(
        @PathVariable String applicationId,
        @Valid @RequestBody ApprovalRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(approvalService.processApproval(applicationId, user.getId(), request));
    }

    @PostMapping("/application/{applicationId}/agent/{agentRole}")
    public ResponseEntity<ApplicationResponse> agentApprove(
        @PathVariable String applicationId,
        @PathVariable String agentRole,
        @RequestParam(required = false) String comment,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(approvalService.agentApprove(applicationId, agentRole, comment));
    }
}
