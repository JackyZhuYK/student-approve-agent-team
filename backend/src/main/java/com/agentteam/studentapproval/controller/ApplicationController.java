package com.agentteam.studentapproval.controller;

import com.agentteam.studentapproval.dto.ApplicationRequest;
import com.agentteam.studentapproval.dto.ApplicationResponse;
import com.agentteam.studentapproval.entity.User;
import com.agentteam.studentapproval.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
        @Valid @RequestBody ApplicationRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(applicationService.createApplication(request, user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(@PathVariable String id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> getUserApplications(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "submittedAt") String sort,
        @RequestParam(defaultValue = "DESC") String direction,
        @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        return ResponseEntity.ok(applicationService.getUserApplications(user.getId(), pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ApplicationResponse>> getAllApplications(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "submittedAt") String sort,
        @RequestParam(defaultValue = "DESC") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        return ResponseEntity.ok(applicationService.getAllApplications(pageable));
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<ApplicationResponse>> getPendingApplications(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        return ResponseEntity.ok(applicationService.getPendingApplications(pageable));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ApplicationResponse> withdrawApplication(
        @PathVariable String id,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(applicationService.withdrawApplication(id, user.getId()));
    }
}
