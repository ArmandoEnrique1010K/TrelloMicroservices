package com.trello.identity.token.service.utils;

import java.util.concurrent.ThreadLocalRandom;

public class TokenUtils {
    private TokenUtils() {
    }

    public static String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

}
