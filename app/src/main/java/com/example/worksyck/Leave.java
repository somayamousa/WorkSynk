package com.example.worksyck;

public class Leave {

    private String id;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String status;
    private String isPaid;
    private String reason;

    // Constructor
    public Leave(String id, String leaveType, String startDate, String endDate, String status, String isPaid, String reason) {
        this.id = id;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.isPaid = isPaid;
        this.reason = reason;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    public String getIsPaid() {
        return isPaid;
    }

    public String getReason() {
        return reason;
    }

    // Setters (if needed)
    public void setId(String id) {
        this.id = id;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setIsPaid(String isPaid) {
        this.isPaid = isPaid;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
