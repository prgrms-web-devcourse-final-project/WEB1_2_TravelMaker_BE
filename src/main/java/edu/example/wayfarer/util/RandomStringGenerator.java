package edu.example.wayfarer.util;

import java.security.SecureRandom;

public class RandomStringGenerator {
    private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomString(int length){
        StringBuilder result = new StringBuilder(length);
        for(int i = 0; i < length; i++){
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }
}
