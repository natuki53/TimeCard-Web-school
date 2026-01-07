package util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * パスワードハッシュ/照合ユーティリティ（bcrypt）
 */
public class PasswordUtil {
    private PasswordUtil() {}

    // コスト（負荷）。学校課題なら 10〜12 程度が無難。
    private static final int COST = 12;

    public static String hash(String rawPassword) {
        if (rawPassword == null) rawPassword = "";
        return BCrypt.withDefaults().hashToString(COST, rawPassword.toCharArray());
    }

    public static boolean verify(String rawPassword, String bcryptHash) {
        if (rawPassword == null) rawPassword = "";
        if (bcryptHash == null) return false;
        BCrypt.Result r = BCrypt.verifyer().verify(rawPassword.toCharArray(), bcryptHash);
        return r.verified;
    }

    public static boolean looksLikeBcryptHash(String s) {
        if (s == null) return false;
        // $2a$, $2b$, $2y$ 等
        return s.startsWith("$2") && s.length() >= 20;
    }
}


