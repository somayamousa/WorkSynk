package com.example.worksyck;

import java.util.ArrayList;
import java.util.List;

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

    private String company_name;
    private String department_name;

    private String designation_name;

    private double baseSalary;

    private  double hourCost;

    private  String SalaryStructureType;
    private double normalHourRate;
    private double overtimeHourRate;
    private double reqularWorkingHour;
    private int workingDaysPerWeek;




    public Employee(String id, String full_name, String email, String department, String company, String designation) {
        this.id = id;
        this.email = email;
        this.fullname = full_name;
        this.department_name=department;
        this.designation_name=designation;
        this.company_name=company;
    }

    public Employee(String email, String password, String fullName, String phone, String selectedStatusValue, Integer integer, int departmentId, int designationId, double baseSalary, double hourCost, String salaryStructure, double normalHourRate, double overTimeRate) {





    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getDepartment_name() {
        return department_name;
    }

    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }

    public String getDesignation_name() {
        return designation_name;
    }

    public void setDesignation_name(String designation_name) {
        this.designation_name = designation_name;
    }



    public Employee(String id, String email, String password, String fullname, String phone, String macAddress, String status, int companyId, int departmentId, int designationId, double baseSalary, double hourCost, String salaryStructureType, double normalHourRate, double overtimeHourRate, double reqularWorkingHour, int workingDaysPerWeek) {
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
        this.reqularWorkingHour=reqularWorkingHour;
        this.workingDaysPerWeek = workingDaysPerWeek;
    }

    public Employee(String email, String password, String fullname, String phone, String status, int companyId, int departmentId, int designationId, double baseSalary, double hourCost, String salaryStructureType, double normalHourRate, double overtimeHourRate,double reqularWorkingHour ,int workingDaysPerWeek) {
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
        this.reqularWorkingHour=reqularWorkingHour;
        this.workingDaysPerWeek = workingDaysPerWeek;
    }

    public int getWorkingDaysPerWeek() {
        return workingDaysPerWeek;
    }

    public void setWorkingDaysPerWeek(int workingDaysPerWeek) {
        this.workingDaysPerWeek = workingDaysPerWeek;
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

    public double getReqularWorkingHour() {
        return reqularWorkingHour;
    }

    public void setReqularWorkingHour(double reqularWorkingHour) {
        this.reqularWorkingHour = reqularWorkingHour;
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

