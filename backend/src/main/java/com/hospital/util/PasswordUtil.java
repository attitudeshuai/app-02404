package com.hospital.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码工具：统一使用 BCrypt 进行哈希与校验。
 */
public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, normalizeHashPrefix(hashedPassword));
        } catch (IllegalArgumentException ex) {
            // 非法哈希格式直接视为不匹配，不做明文回退
            return false;
        }
    }

    /**
     * jBCrypt 主要使用 $2a$ 前缀；对外部产生的 $2b$/$2y$ 哈希做前缀兼容。
     */
    private static String normalizeHashPrefix(String hashedPassword) {
        if (hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$")) {
            return "$2a$" + hashedPassword.substring(4);
        }
        return hashedPassword;
    }
}
