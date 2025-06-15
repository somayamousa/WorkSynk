package com.example.worksyck;

import java.util.ArrayList;
import java.util.List;

public class Employee {
    private String id;
    private String email;
    private String password;
    private String fullname;
    private String phone;
    private String androidId; // Changed from macAddress to androidId for consistency
    private String status;
    private int companyId;
    private int departmentId;
    private int designationId;
    private String companyName;
    private String departmentName;
    private String designationName;
    private double baseSalary;
    private double hourDeductionCost; // Changed from hourCost for clarity
    private String salaryStructureType;
    private double normalHourRate;
    private double overtimeHourRate;
    private double expectedHoursPerDay; // Changed from reqularWorkingHour for clarity
    private int workingDaysPerWeek;
    private String employeeCode;
    private List<String> workingDays;

    // Constructor for minimal employee data (e.g., for display purposes)
    public Employee(String id, String fullname, String email, String departmentName, String companyName, String designationName) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.departmentName = departmentName;
        this.companyName = companyName;
        this.designationName = designationName;
        this.workingDays = new ArrayList<>();
    }

    // Constructor with employee code
    public Employee(String id, String fullname, String email, String departmentName, String companyName, String designationName, String employeeCode) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.departmentName = departmentName;
        this.companyName = companyName;
        this.designationName = designationName;
        this.employeeCode = employeeCode;
        this.workingDays = new ArrayList<>();
    }

    // Full constructor for employee creation
    public Employee(String email, String password, String fullname, String phone, String status, int companyId,
                    int departmentId, int designationId, double baseSalary, double hourDeductionCost,
                    String salaryStructureType, double normalHourRate, double overtimeHourRate,
                    double expectedHoursPerDay, int workingDaysPerWeek, List<String> workingDays) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.phone = phone;
        this.status = status;
        this.companyId = companyId;
        this.departmentId = departmentId;
        this.designationId = designationId;
        this.baseSalary = baseSalary;
        this.hourDeductionCost = hourDeductionCost;
        this.salaryStructureType = salaryStructureType;
        this.normalHourRate = normalHourRate;
        this.overtimeHourRate = overtimeHourRate;
        this.expectedHoursPerDay = expectedHoursPerDay;
        this.workingDaysPerWeek = workingDaysPerWeek;
        this.workingDays = workingDays != null ? workingDays : new ArrayList<>();
        this.employeeCode = "";
        this.androidId = "";
    }

    // Full constructor with androidId and employeeCode
    public Employee(String id, String email, String password, String fullname, String phone, String androidId,
                    String status, int companyId, int departmentId, int designationId, double baseSalary,
                    double hourDeductionCost, String salaryStructureType, double normalHourRate,
                    double overtimeHourRate, double expectedHoursPerDay, int workingDaysPerWeek,
                    String employeeCode, List<String> workingDays) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.phone = phone;
        this.androidId = androidId;
        this.status = status;
        this.companyId = companyId;
        this.departmentId = departmentId;
        this.designationId = designationId;
        this.baseSalary = baseSalary;
        this.hourDeductionCost = hourDeductionCost;
        this.salaryStructureType = salaryStructureType;
        this.normalHourRate = normalHourRate;
        this.overtimeHourRate = overtimeHourRate;
        this.expectedHoursPerDay = expectedHoursPerDay;
        this.workingDaysPerWeek = workingDaysPerWeek;
        this.employeeCode = employeeCode;
        this.workingDays = workingDays != null ? workingDays : new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getDesignationId() {
        return designationId;
    }

    public void setDesignationId(int designationId) {
        this.designationId = designationId;
    }

    public String getCompany_name() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDepartment_name() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDesignation_name() {
        return designationName;
    }

    public void setDesignationName(String designationName) {
        this.designationName = designationName;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public double getHourDeductionCost() {
        return hourDeductionCost;
    }

    public void setHourDeductionCost(double hourDeductionCost) {
        this.hourDeductionCost = hourDeductionCost;
    }

    public String getSalaryStructureType() {
        return salaryStructureType;
    }

    public void setSalaryStructureType(String salaryStructureType) {
        this.salaryStructureType = salaryStructureType;
    }

    public double getNormalHourRate() {
        return normalHourRate;
    }

    public void setNormalHourRate(double normalHourRate) {
        this.normalHourRate = normalHourRate;
    }

    public double getOvertimeHourRate() {
        return overtimeHourRate;
    }

    public void setOvertimeHourRate(double overtimeHourRate) {
        this.overtimeHourRate = overtimeHourRate;
    }

    public double getExpectedHoursPerDay() {
        return expectedHoursPerDay;
    }

    public void setExpectedHoursPerDay(double expectedHoursPerDay) {
        this.expectedHoursPerDay = expectedHoursPerDay;
    }

    public int getWorkingDaysPerWeek() {
        return workingDaysPerWeek;
    }

    public void setWorkingDaysPerWeek(int workingDaysPerWeek) {
        this.workingDaysPerWeek = workingDaysPerWeek;
    }

    public String getEmployee_code() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }
    public List<String> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(List<String> workingDays) {
        this.workingDays = workingDays != null ? workingDays : new ArrayList<>();
    }
}