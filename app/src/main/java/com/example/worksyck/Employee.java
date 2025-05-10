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

    // Constructor
    public Employee(String email, String password, String fullname, String phone,
                    String macAddress, String status, int companyId,
                    int departmentId, int designationId) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.phone = phone;
        this.macAddress = macAddress;
        this.status = status;
        this.companyId = companyId;
        this.departmentId = departmentId;
        this.designationId = designationId;
    }

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
}

