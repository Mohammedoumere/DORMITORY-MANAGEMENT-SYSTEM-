package com.dormitory.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@gmail\\.com$";
        return Pattern.matches(emailRegex, email);
    }
    
    public static boolean isValidEthiopianPhone(String phone) {
        if (phone == null) return false;
        // Ethiopian phone format: +251XXXXXXXXX or 09XXXXXXXX
        String phoneRegex = "^(\\+2519|09)\\d{8}$";
        return Pattern.matches(phoneRegex, phone);
    }
    
    public static boolean isValidPassword(String password) {
        // At least 6 characters
        return password != null && password.length() >= 6;
    }
    
    public static boolean isValidStudentId(String id) {
        // Format: S/UR/XXXX/YY
        return id != null && id.matches("^S/UR/\\d{4}/\\d{2}$");
    }
}