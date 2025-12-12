package dao;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * ログイン保持（Remember me）トークンDAO
 */
public class RememberTokenDAO {
    private final String JDBC_URL = "jdbc:mysql://localhost:3306/timecard_db?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8";
    private final String DB_USER = "root";
    private final String DB_PASS = "";

    public static class RememberToken {
        public final int userId;
        public final String rawToken;
        public final LocalDateTime expiresAt;

        public RememberToken(int userId, String rawToken, LocalDateTime expiresAt) {
            this.userId = userId;
            this.rawToken = rawToken;
            this.expiresAt = expiresAt;
        }
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("ハッシュ生成に失敗しました", e);
        }
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * トークンを発行（DBにはハッシュのみ保存）
     * @param userId ユーザーID
     * @param days 有効日数
     */
    public RememberToken issueToken(int userId, int days) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        String raw = newToken();
        String hash = sha256Hex(raw);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(days);

        String sql = "INSERT INTO remember_tokens(user_id, token_hash, expires_at) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, hash);
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(expiresAt));
            ps.executeUpdate();
            return new RememberToken(userId, raw, expiresAt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * トークンから user_id を取得（期限切れは無効）
     */
    public Integer findUserIdByToken(String rawToken) {
        if (rawToken == null || rawToken.trim().isEmpty()) return null;

        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        String hash = sha256Hex(rawToken.trim());
        String sql = "SELECT user_id FROM remember_tokens WHERE token_hash = ? AND expires_at > NOW()";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * トークンを失効（ログアウト用）
     */
    public boolean revokeToken(String rawToken) {
        if (rawToken == null || rawToken.trim().isEmpty()) return false;

        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        String hash = sha256Hex(rawToken.trim());
        String sql = "DELETE FROM remember_tokens WHERE token_hash = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

