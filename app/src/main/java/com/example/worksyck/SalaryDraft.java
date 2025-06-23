package com.example.worksyck;

import java.io.Serializable;

public class SalaryDraft implements Serializable {
    private int id;
    private int employeeId;
    private String employeeName;
    private String employeeCode;
    private double netSalary;
    private double baseSalary;
    private String periodStart;
    private String periodEnd;
    private String salaryStructureType;
    private String departmentName;
    private String designationName;
    private double regularHours;
    private double regularHourRate;
    private double regularSalary;
    private double overtimeHours;
    private double overtimeHourRate;
    private double overtimeSalary;
    private double bonus;
    private double salaryIncrement;
    private int expectedWorkingDays;
    private int absentDays;
    private String status;

    public SalaryDraft(int id, int employeeId, String employeeName, String employeeCode, double netSalary,
                       double baseSalary, String periodStart, String periodEnd, String salaryStructureType,
                       String departmentName, String designationName, double regularHours, double regularHourRate,
                       double regularSalary, double overtimeHours, double overtimeHourRate, double overtimeSalary,
                       double bonus, double salaryIncrement, int expectedWorkingDays, int absentDays, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.netSalary = netSalary;
        this.baseSalary = baseSalary;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.salaryStructureType = salaryStructureType;
        this.departmentName = departmentName;
        this.designationName = designationName;
        this.regularHours = regularHours;
        this.regularHourRate = regularHourRate;
        this.regularSalary = regularSalary;
        this.overtimeHours = overtimeHours;
        this.overtimeHourRate = overtimeHourRate;
        this.overtimeSalary = overtimeSalary;
        this.bonus = bonus;
        this.salaryIncrement = salaryIncrement;
        this.expectedWorkingDays = expectedWorkingDays;
        this.absentDays = absentDays;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeCode() { return employeeCode; }
    public double getNetSalary() { return netSalary; }
    public double getBaseSalary() { return baseSalary; }
    public String getPeriodStart() { return periodStart; }
    public String getPeriodEnd() { return periodEnd; }
    public String getSalaryStructureType() { return salaryStructureType; }
    public String getDepartmentName() { return departmentName; }
    public String getDesignationName() { return designationName; }
    public double getRegularHours() { return regularHours; }
    public double getRegularHourRate() { return regularHourRate; }
    public double getRegularSalary() { return regularSalary; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getOvertimeHourRate() { return overtimeHourRate; }
    public double getOvertimeSalary() { return overtimeSalary; }
    public double getBonus() { return bonus; }
    public double getSalaryIncrement() { return salaryIncrement; }
    public int getExpectedWorkingDays() { return expectedWorkingDays; }
    public int getAbsentDays() { return absentDays; }
    public String getStatus() { return status; }

}