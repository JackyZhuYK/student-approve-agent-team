package com.agentteam.studentapproval.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private String id;
    private String title;
    private String description;
    private String type;
    private String status;
    private String priority;
    private List<String> attachmentUrls;
    private UserInfo applicant;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private Boolean withdrawn;
    private List<ApprovalDTO> approvals;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String username;
        private String displayName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalDTO {
        private String id;
        private String approverName;
        private String agentRole;
        private String action;
        private String comment;
        private LocalDateTime createdAt;
    }
}
