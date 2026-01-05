package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * トークン生成・ハッシュ計算ユーティリティ
 */
public class CryptoUtil {
    private static final SecureRandom RNG = new SecureRandom();

    private CryptoUtil() {}

    /**
     * URLに安全なランダムトークン（Base64URL, no padding）
     */
    public static String newUrlSafeToken(int bytes) {
        if (bytes <= 0) bytes = 32;
        byte[] b = new byte[bytes];
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    /**
     * SHA-256 を 16進(小文字)で返す
     */
    public static String sha256Hex(String s) {
        if (s == null) s = "";
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 が利用できません", e);
        }
    }

    private static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            sb.append(Character.forDigit((x >> 4) & 0xF, 16));
            sb.append(Character.forDigit(x & 0xF, 16));
        }
        return sb.toString();
    }
}


