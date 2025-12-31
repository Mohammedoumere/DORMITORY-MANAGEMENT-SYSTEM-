package com.dormitory.models;

public class Proctor extends User {
    private String blockId; // Renamed from 'block' to match database and controller
    private String phoneNumber;
    private String gender;
    private String status; // Added missing status field

    public Proctor() {
        this.userType = "PROCTOR";
    }
    
    // Getters and Setters
    public String getBlockId() { return blockId; }
    public void setBlockId(String blockId) { this.blockId = blockId; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}