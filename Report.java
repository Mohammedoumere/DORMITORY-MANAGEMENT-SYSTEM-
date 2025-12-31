package com.dormitory.models;

import java.time.LocalDateTime;

public class Report {
    private int reportId;
    private String submittedById;
    private String submittedByRole;
    private String submittedToId;
    private String title;
    private String problemDescription;
    private String status;
    private String reply;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for display purposes
    private String submittedByName;
    private String submittedByBlockId; // New field for the block ID

    // Getters and Setters
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public String getSubmittedById() { return submittedById; }
    public void setSubmittedById(String submittedById) { this.submittedById = submittedById; }

    public String getSubmittedByRole() { return submittedByRole; }
    public void setSubmittedByRole(String submittedByRole) { this.submittedByRole = submittedByRole; }

    public String getSubmittedToId() { return submittedToId; }
    public void setSubmittedToId(String submittedToId) { this.submittedToId = submittedToId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getSubmittedByName() { return submittedByName; }
    public void setSubmittedByName(String submittedByName) { this.submittedByName = submittedByName; }

    public String getSubmittedByBlockId() { return submittedByBlockId; }
    public void setSubmittedByBlockId(String submittedByBlockId) { this.submittedByBlockId = submittedByBlockId; }
}
