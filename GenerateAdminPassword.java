package com.dormitory.utils;

public class GenerateAdminPassword {
    public static void main(String[] args) {
        // This will generate a new, correct hash for the password "password"
        String newHash = PasswordUtils.hashPassword("password");
        System.out.println("New Admin Hash: " + newHash);
    }
}
