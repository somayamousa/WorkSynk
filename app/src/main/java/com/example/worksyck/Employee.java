package com.example.worksyck;

public class Employee {
    private String id;
    private String email;
    private String password;
    private String fullname;
    private String phone;
    private String macAddress;
    private String status;
    private int companyId;
    private int departmentId;

    private int designationId;

    private double baseSalary;

    private  double hourCost;

    private  String SalaryStructureType;
    private double normalHourRate;
    private double overtimeHourRate;

    public Employee(String id, String email, String password, String fullname, String phone, String macAddress, String status, int companyId, int departmentId, int designationId, double baseSalary, double hourCost, String salaryStructureType, double normalHourRate, double overtimeHourRate) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.phone = phone;
        this.macAddress = macAddress;
        this.status = status;
        this.companyId = companyId;
        this.departmentId = departmentId;
        this.designationId = designationId;
        this.baseSalary = baseSalary;
        this.hourCost = hourCost;
        SalaryStructureType = salaryStructureType;
        this.normalHourRate = normalHourRate;
        this.overtimeHourRate = overtimeHourRate;
    }

    public Employee(String email, String password, String fullname, String phone, String status, int companyId, int departmentId, int designationId, double baseSalary, double hourCost, String salaryStructureType, double normalHourRate, double overtimeHourRate) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.phone = phone;
        this.macAddress = macAddress;
        this.status = status;
        this.companyId = companyId;
        this.departmentId = departmentId;
        this.designationId = designationId;
        this.baseSalary = baseSalary;
        this.hourCost = hourCost;
        SalaryStructureType = salaryStructureType;
        this.normalHourRate = normalHourRate;
        this.overtimeHourRate = overtimeHourRate;
    }


    public String getSalaryStructureType() {
        return SalaryStructureType;
    }

    public double getHourCost() {
        return hourCost;
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

    public void setSalaryStructureType(String salaryStructureType) {
        SalaryStructureType = salaryStructureType;
    }

    public void setHourCost(double hourCost) {
        this.hourCost = hourCost;
    }
    // Constructor




    // Getters and Setters
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

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
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


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }
}

