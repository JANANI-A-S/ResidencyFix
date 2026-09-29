package com.residencyfix.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Resident name is required")
    @Column(nullable = false)
    private String residentName;

    @NotBlank(message = "Room number is required")
    @Column(nullable = false)
    private String roomNumber;

    @NotBlank(message = "Complaint category is required")
    @Column(nullable = false)
    private String category;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    @Column(nullable = false, length = 500)
    private String description;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status;

    @Column(length = 500)
    private String statusNote;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Relationships suggested in problem statement
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resident_id")
    @JsonIgnoreProperties("complaints")
    private Resident resident;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("complaints")
    private Category categoryEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_id")
    @JsonIgnoreProperties("assignedComplaints")
    private Staff assignedStaff;

    public Complaint() {
        this.createdAt = LocalDateTime.now();
        this.status = ComplaintStatus.NEW;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResidentName() {
        if (resident != null && (residentName == null || residentName.isBlank())) {
            return resident.getName();
        }
        return residentName;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public String getRoomNumber() {
        if (resident != null && (roomNumber == null || roomNumber.isBlank())) {
            return resident.getRoomNumber();
        }
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getCategory() {
        if (categoryEntity != null && (category == null || category.isBlank())) {
            return categoryEntity.getName();
        }
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getStatusNote() {
        return statusNote;
    }

    public void setStatusNote(String statusNote) {
        this.statusNote = statusNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Resident getResident() {
        return resident;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
        if (resident != null) {
            this.residentName = resident.getName();
            if (this.roomNumber == null || this.roomNumber.isBlank()) {
                this.roomNumber = resident.getRoomNumber();
            }
        }
    }

    public Category getCategoryEntity() {
        return categoryEntity;
    }

    public void setCategoryEntity(Category categoryEntity) {
        this.categoryEntity = categoryEntity;
        if (categoryEntity != null) {
            this.category = categoryEntity.getName();
        }
    }

    public Staff getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(Staff assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    // Auto-flag complaints open for more than 5 days as overdue (PS Requirement)
    public boolean isOverdue() {
        return status != ComplaintStatus.RESOLVED
                && status != ComplaintStatus.REJECTED
                && createdAt != null
                && createdAt.isBefore(LocalDateTime.now().minusDays(5));
    }

    public long getDaysOpen() {
        if (createdAt == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(createdAt.toLocalDate(), LocalDateTime.now().toLocalDate());
    }

    public String getStatusLabel() {
        return status == null ? "Open" : status.getLabel();
    }

    public String getStatusClass() {
        if (status == null) return "badge-new";
        return switch (status) {
            case NEW -> "badge-new";
            case IN_PROGRESS -> "badge-progress";
            case RESOLVED -> "badge-resolved";
            case REJECTED -> "badge-rejected";
        };
    }

    public String getCategoryIcon() {
        if (category == null) return "📋";
        return switch (category.toLowerCase()) {
            case "plumbing" -> "🚰";
            case "electrical" -> "⚡";
            case "cleaning" -> "🧹";
            default -> "📋";
        };
    }

    public String getCategoryClass() {
        if (category == null) return "cat-other";
        return switch (category.toLowerCase()) {
            case "plumbing" -> "cat-plumbing";
            case "electrical" -> "cat-electrical";
            case "cleaning" -> "cat-cleaning";
            default -> "cat-other";
        };
    }

    public int getStatusStepOrder() {
        if (status == null) return 1;
        return switch (status) {
            case NEW -> 1;
            case IN_PROGRESS -> 2;
            case RESOLVED -> 3;
            case REJECTED -> 4;
        };
    }
}
