package com.dormitory.models;

import java.time.LocalDate;

public class Student extends User {
    private int age;
    private String gender;
    private int yearOfStudy;
    private String college;
    private String department;
    private String nationality;
    private String region;
    private String city;
    private String phoneNumber;
    private String blockId;
    private String roomNumber;
    // private int bedNumber; // Removed
    private String residentFullName;
    private String residentRelation;
    private String residentPhone;
    private String status;
    
    public Student() {
        this.userType = "STUDENT";
    }
    
    // Getters and Setters
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getBlockId() { return blockId; }
    public void setBlockId(String blockId) { this.blockId = blockId; }
    
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    // public int getBedNumber() { return bedNumber; }
    // public void setBedNumber(int bedNumber) { this.bedNumber = bedNumber; }
    
    public String getResidentFullName() { return residentFullName; }
    public void setResidentFullName(String residentFullName) { this.residentFullName = residentFullName; }
    
    public String getResidentRelation() { return residentRelation; }
    public void setResidentRelation(String residentRelation) { this.residentRelation = residentRelation; }
    
    public String getResidentPhone() { return residentPhone; }
    public void setResidentPhone(String residentPhone) { this.residentPhone = residentPhone; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void setRegistrationDate(LocalDate now) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
