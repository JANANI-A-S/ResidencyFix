package com.residencyfix.dto;

import com.residencyfix.model.ComplaintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ComplaintDTO {

    private Long id;

    private String residentName;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    private ComplaintStatus status;

    private String statusNote;

    public ComplaintDTO() {
    }

    public ComplaintDTO(Long id, String residentName, String roomNumber, String category, String description, ComplaintStatus status, String statusNote) {
        this.id = id;
        this.residentName = residentName;
        this.roomNumber = roomNumber;
        this.category = category;
        this.description = description;
        this.status = status;
        this.statusNote = statusNote;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResidentName() {
        return residentName;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getCategory() {
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
}
