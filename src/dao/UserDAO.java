package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.User;

import util.DBUtil;
import util.PasswordUtil;

/**
 * ユーザーDAOクラス（データベースアクセス）
 */
public class UserDAO {
    
    /**
     * ログインIDとパスワードでユーザーを検索
     * @param loginId ログインID
     * @param password パスワード
     * @return 見つかったユーザー、見つからなければnull
     */
    public User findByLoginIdAndPassword(String loginId, String password) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        
        // データベースに接続してユーザー検索を実行
        try(Connection conn = DBUtil.getConnection()) {
            // login_id で取得し、パスワードはアプリ側で照合する（bcrypt / 旧平文互換）
            String sql = "SELECT id, login_id, password_hash, secret_question, secret_answer_hash, name, bio, dm_allowed, icon_filename, is_deleted, deleted_at "
                       + "FROM users WHERE login_id = ? AND is_deleted = 0";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loginId);
                try (ResultSet rs = ps.executeQuery()) {
                    if(!rs.next()) return null;

                    String stored = rs.getString("password_hash");
                    boolean ok;
                    if (PasswordUtil.looksLikeBcryptHash(stored)) {
                        ok = PasswordUtil.verify(password, stored);
                    } else {
                        // 旧データ互換: 平文が入っている場合
                        ok = stored != null && stored.equals(password);
                        if (ok) {
                            // 自動移行（ログイン成功時に bcrypt へ）
                            String newHash = PasswordUtil.hash(password);
                            try (PreparedStatement ups = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
                                ups.setString(1, newHash);
                                ups.setInt(2, rs.getInt("id"));
                                ups.executeUpdate();
                            }
                            stored = newHash;
                        }
                    }
                    if (!ok) return null;

                User user = new User();
                user.setId(rs.getInt("id"));
                user.setLoginId(rs.getString("login_id"));
                    user.setPasswordHash(stored);
                    user.setSecretQuestion(rs.getString("secret_question"));
                    user.setSecretAnswerHash(rs.getString("secret_answer_hash"));
                user.setName(rs.getString("name"));
                    user.setBio(rs.getString("bio"));
                    user.setDmAllowed(rs.getInt("dm_allowed") == 1);
                user.setIconFilename(rs.getString("icon_filename"));
                user.setDeleted(rs.getInt("is_deleted") == 1);
                if (rs.getTimestamp("deleted_at") != null) {
                    user.setDeletedAt(rs.getTimestamp("deleted_at").toLocalDateTime());
                }
                return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // ユーザーが見つからない場合はnullを返す
        return null;
    }
    
    /**
     * 新規ユーザーを登録
     * @param user 登録するユーザー
     * @return 登録成功したらtrue
     */
    public boolean insert(User user) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        
        return insertAndReturnId(user) > 0;
        
        // 登録失敗の場合はfalseを返す
    }

    /**
     * 新規ユーザーを登録し、生成された user_id を返す
     * 失敗時は -1
     */
    public int insertAndReturnId(User user) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        String sql = "INSERT INTO users(login_id, password_hash, secret_question, secret_answer_hash, name, dm_allowed) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getLoginId());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSecretQuestion());
            ps.setString(4, user.getSecretAnswerHash());
            ps.setString(5, user.getName());
            ps.setInt(6, user.isDmAllowed() ? 1 : 0);
            int updated = ps.executeUpdate();
            if (updated <= 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * ログインIDでユーザーを検索
     * @param loginId ログインID
     * @return 見つかったユーザー、見つからなければnull
     */
    public User findByLoginId(String loginId) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        
        // データベースに接続してユーザー検索を実行
        try(Connection conn = DBUtil.getConnection()) {
            // ログインIDでユーザーを検索するSQL文
            String sql = "SELECT id, login_id, password_hash, secret_question, secret_answer_hash, name, bio, dm_allowed, icon_filename, is_deleted, deleted_at "
                       + "FROM users WHERE login_id = ? AND is_deleted = 0";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // SQL文のプレースホルダーに値を設定
            ps.setString(1, loginId);
            
            // SQL文を実行してユーザー情報を取得
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                // 検索結果からUserオブジェクトを作成
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setLoginId(rs.getString("login_id"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setSecretQuestion(rs.getString("secret_question"));
                user.setSecretAnswerHash(rs.getString("secret_answer_hash"));
                user.setName(rs.getString("name"));
                user.setBio(rs.getString("bio"));
                user.setDmAllowed(rs.getInt("dm_allowed") == 1);
                user.setIconFilename(rs.getString("icon_filename"));
                user.setDeleted(rs.getInt("is_deleted") == 1);
                if (rs.getTimestamp("deleted_at") != null) {
                    user.setDeletedAt(rs.getTimestamp("deleted_at").toLocalDateTime());
                }
                return user;
            }
        } catch (SQLException e) {
            // データベースエラーをコンソールに出力
            e.printStackTrace();
        }
        
        // ユーザーが見つからない場合はnullを返す
        return null;
    }
    
    /**
     * ユーザーIDでユーザーを検索
     * @param userId ユーザーID
     * @return 見つかったユーザー、見つからなければnull
     */
    public User findById(int userId) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        
        // データベースに接続してユーザー検索を実行
        try(Connection conn = DBUtil.getConnection()) {
            // ユーザーIDでユーザーを検索するSQL文
            String sql = "SELECT id, login_id, password_hash, secret_question, secret_answer_hash, name, bio, dm_allowed, icon_filename, is_deleted, deleted_at "
                       + "FROM users WHERE id = ? AND is_deleted = 0";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // SQL文のプレースホルダーに値を設定
            ps.setInt(1, userId);
            
            // SQL文を実行してユーザー情報を取得
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                // 検索結果からUserオブジェクトを作成
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setLoginId(rs.getString("login_id"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setSecretQuestion(rs.getString("secret_question"));
                user.setSecretAnswerHash(rs.getString("secret_answer_hash"));
                user.setName(rs.getString("name"));
                user.setBio(rs.getString("bio"));
                user.setDmAllowed(rs.getInt("dm_allowed") == 1);
                user.setIconFilename(rs.getString("icon_filename"));
                user.setDeleted(rs.getInt("is_deleted") == 1);
                if (rs.getTimestamp("deleted_at") != null) {
                    user.setDeletedAt(rs.getTimestamp("deleted_at").toLocalDateTime());
                }
                return user;
            }
        } catch (SQLException e) {
            // データベースエラーをコンソールに出力
            e.printStackTrace();
        }
        
        // ユーザーが見つからない場合はnullを返す
        return null;
    }

    /**
     * ログインID（部分一致）でユーザー候補を検索（グループ既存メンバーは除外）
     * - グループ管理画面のオートコンプリート用途
     * @param query 入力文字列
     * @param groupId 対象グループID
     * @param limit 最大件数（1以上推奨）
     * @return ユーザー候補（id, loginId, nameのみ設定）
     */
    public List<User> findSuggestionsByLoginId(String query, int groupId, int limit) {
        List<User> users = new ArrayList<>();

        if (query == null) return users;
        String q = query.trim();
        if (q.isEmpty()) return users;
        if (limit <= 0) limit = 10;

        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        // login_id 部分一致 + 既に group_members にいる人を除外
        // 例: "ab" -> "%ab%"
        String like = "%" + q + "%";
        String sql = "SELECT u.id, u.login_id, u.name "
                   + "FROM users u "
                   + "LEFT JOIN group_members gm ON gm.group_id = ? AND gm.user_id = u.id "
                   + "WHERE gm.user_id IS NULL AND u.is_deleted = 0 AND u.login_id LIKE ? "
                   + "ORDER BY (u.login_id = ?) DESC, u.login_id ASC "
                   + "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, like);
            ps.setString(3, q);
            ps.setInt(4, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setLoginId(rs.getString("login_id"));
                    u.setName(rs.getString("name"));
                    users.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     * プロフィール表示用（公開情報のみ）
     */
    public User findPublicProfileById(int userId) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        String sql = "SELECT id, login_id, name, bio, dm_allowed, icon_filename, is_deleted, deleted_at "
                   + "FROM users WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                if (rs.getInt("is_deleted") == 1) return null;

                User u = new User();
                u.setId(rs.getInt("id"));
                u.setLoginId(rs.getString("login_id"));
                u.setName(rs.getString("name"));
                u.setBio(rs.getString("bio"));
                u.setDmAllowed(rs.getInt("dm_allowed") == 1);
                u.setIconFilename(rs.getString("icon_filename"));
                u.setDeleted(false);
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * プロフィール更新（表示名・自己紹介・アイコン）
     * iconFilename が null の場合はアイコンは更新しない。
     */
    public boolean updateProfile(int userId, String name, String bio, boolean dmAllowed, String iconFilename) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        String sql;
        if (iconFilename == null) {
            sql = "UPDATE users SET name = ?, bio = ?, dm_allowed = ? WHERE id = ? AND is_deleted = 0";
        } else {
            sql = "UPDATE users SET name = ?, bio = ?, dm_allowed = ?, icon_filename = ? WHERE id = ? AND is_deleted = 0";
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            if (iconFilename == null) {
                ps.setString(2, bio);
                ps.setInt(3, dmAllowed ? 1 : 0);
                ps.setInt(4, userId);
            } else {
                ps.setString(2, bio);
                ps.setInt(3, dmAllowed ? 1 : 0);
                ps.setString(4, iconFilename);
                ps.setInt(5, userId);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ユーザーを論理削除（退会）
     */
    public boolean softDelete(int userId) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        String sql = "UPDATE users SET is_deleted = 1, deleted_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 秘密の質問の回答ハッシュを更新
     */
    public boolean updateSecretAnswerHash(int userId, String newSecretAnswerHash) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        String sql = "UPDATE users SET secret_answer_hash = ? WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newSecretAnswerHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * パスワード更新
     */
    public boolean updatePasswordHash(int userId, String newPasswordHash) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        String sql = "UPDATE users SET password_hash = ? WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

