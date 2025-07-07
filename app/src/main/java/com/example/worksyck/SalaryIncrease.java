package com.example.worksyck;

import java.time.LocalDate;

public class SalaryIncrease {


    private int id;
    private int employeeId;
    private double increaseAmount;
    private String increaseType;
    private String durationType; // Temporary or Permanent
    private String startDate;
    private String endDate; // Nullable
    private String notes;
    private String createdAt;
    private  String updated_at;

    // Constructor
    public SalaryIncrease(int id, int employeeId, double increaseAmount, String increaseType,
                          String durationType, String startDate, String endDate, String notes, String createdAt ,String updated_at) {
        this.id = id;
        this.employeeId = employeeId;
        this.increaseAmount = increaseAmount;
        this.increaseType = increaseType;
        this.durationType = durationType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
        this.updated_at=updated_at;
        this.createdAt = createdAt;
    }
    public SalaryIncrease(int id, double increaseAmount, String increaseType,
                          String durationType, String startDate, String endDate, String notes, String createdAt,String updated_at) {
        this.id = id;

        this.increaseAmount = increaseAmount;
        this.increaseType = increaseType;
        this.durationType = durationType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
        this.updated_at=updated_at;
        this.createdAt = createdAt;
    }

    public SalaryIncrease(String type, String durationType,double amount, String increasestartDate) {
        this.increaseAmount=amount;
        this.durationType=durationType;
        this.increaseType=type;
        this.startDate=increasestartDate;
    }

    public SalaryIncrease(String type, String durationType, double applicableAmount, String s, String s1) {

        this.increaseAmount=applicableAmount;
        this.durationType=durationType;
        this.increaseType=type;
        this.startDate=s;
        this.endDate=s1;
    }

    // Getters and Setters


    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public double getIncreaseAmount() {
        return increaseAmount;
    }

    public void setIncreaseAmount(double increaseAmount) {
        this.increaseAmount = increaseAmount;
    }

    public String getIncreaseType() {
        return increaseType;
    }

    public void setIncreaseType(String increaseType) {
        this.increaseType = increaseType;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    @Override
    public String toString() {
        return "SalaryIncrease{" +
                "increaseAmount=" + increaseAmount +
                ", id=" + id +
                ", employeeId=" + employeeId +
                ", increaseType='" + increaseType + '\'' +
                ", durationType='" + durationType + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
