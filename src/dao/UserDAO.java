package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.User;

/**
 * ユーザーDAOクラス（データベースアクセス）
 */
public class UserDAO {
    
    // データベース接続情報の定数定義
    private final String JDBC_URL = "jdbc:mysql://localhost:3306/timecard_db?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8";
    private final String DB_USER = "root";
    private final String DB_PASS = "";
    
    /**
     * ログインIDとパスワードでユーザーを検索
     * @param loginId ログインID
     * @param password パスワード
     * @return 見つかったユーザー、見つからなければnull
     */
    public User findByLoginIdAndPassword(String loginId, String password) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        
        // データベースに接続してユーザー検索を実行
        try(Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            // ログインIDとパスワードでユーザーを検索するSQL文
            String sql = "SELECT id, login_id, password_hash, name FROM users WHERE login_id = ? AND password_hash = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // SQL文のプレースホルダーに値を設定
            ps.setString(1, loginId);
            ps.setString(2, password);
            
            // SQL文を実行してユーザー情報を取得
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                // 検索結果からUserオブジェクトを作成
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setLoginId(rs.getString("login_id"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setName(rs.getString("name"));
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
     * 新規ユーザーを登録
     * @param user 登録するユーザー
     * @return 登録成功したらtrue
     */
    public boolean insert(User user) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        
        // データベースに接続してユーザー登録を実行
        try(Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            // 新規ユーザーを登録するSQL文
            String sql = "INSERT INTO users(login_id, password_hash, name) VALUES(?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // SQL文のプレースホルダーに値を設定
            ps.setString(1, user.getLoginId());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getName());
            
            // SQL文を実行して登録処理を行う
            int result = ps.executeUpdate();
            // 1行以上更新されれば登録成功
            return result > 0;
        } catch (SQLException e) {
            // データベースエラーをコンソールに出力
            e.printStackTrace();
        }
        
        // 登録失敗の場合はfalseを返す
        return false;
    }
    
    /**
     * ログインIDでユーザーを検索
     * @param loginId ログインID
     * @return 見つかったユーザー、見つからなければnull
     */
    public User findByLoginId(String loginId) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        
        // データベースに接続してユーザー検索を実行
        try(Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            // ログインIDでユーザーを検索するSQL文
            String sql = "SELECT id, login_id, password_hash, name FROM users WHERE login_id = ?";
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
                user.setName(rs.getString("name"));
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
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        
        // データベースに接続してユーザー検索を実行
        try(Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            // ユーザーIDでユーザーを検索するSQL文
            String sql = "SELECT id, login_id, password_hash, name FROM users WHERE id = ?";
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
                user.setName(rs.getString("name"));
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
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        // login_id 部分一致 + 既に group_members にいる人を除外
        // 例: "ab" -> "%ab%"
        String like = "%" + q + "%";
        String sql = "SELECT u.id, u.login_id, u.name "
                   + "FROM users u "
                   + "LEFT JOIN group_members gm ON gm.group_id = ? AND gm.user_id = u.id "
                   + "WHERE gm.user_id IS NULL AND u.login_id LIKE ? "
                   + "ORDER BY (u.login_id = ?) DESC, u.login_id ASC "
                   + "LIMIT ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
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
}

