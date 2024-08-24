package com.smart.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OTPService {
    private final Random random = new Random();
    private final Map<String, String> otpStorage = new HashMap<>(); // In-memory storage for demo purposes

    public String generateOTP(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStorage.put(email, otp);
        // Logic to send OTP to email
        // For example: emailService.sendOtp(email, otp);
        return otp;
    }

    public boolean verifyOTP(String email, String otp) {
        return otp.equals(otpStorage.get(email));
    }

    public void clearOTP(String email) {
        otpStorage.remove(email);
    }
}

