package com.trello.identity.token.service.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TokenUtils {
    private TokenUtils() {
    }

    public static String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    public static UUID generateResetToken() {
        return UUID.randomUUID();
    }

}
