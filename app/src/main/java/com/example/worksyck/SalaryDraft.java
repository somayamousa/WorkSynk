package com.example.worksyck;

import java.io.Serializable;

public class SalaryDraft implements Serializable {
    private int id;
    private int employeeId;
    private String employeeName;
    private String employeeCode;
    private double netSalary;
    private String periodStart;
    private String periodEnd;
    private String salaryStructureType;
    private String departmentName;
    private String designationName;

    public SalaryDraft(int id, int employeeId, String employeeName, String employeeCode, double netSalary,
                       String periodStart, String periodEnd, String salaryStructureType,
                       String departmentName, String designationName) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.netSalary = netSalary;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.salaryStructureType = salaryStructureType;
        this.departmentName = departmentName;
        this.designationName = designationName;
    }

    // Getters
    public int getId() { return id; }
    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeCode() { return employeeCode; }
    public double getNetSalary() { return netSalary; }
    public String getPeriodStart() { return periodStart; }
    public String getPeriodEnd() { return periodEnd; }
    public String getSalaryStructureType() { return salaryStructureType; }
    public String getDepartmentName() { return departmentName; }
    public String getDesignationName() { return designationName; }
}
