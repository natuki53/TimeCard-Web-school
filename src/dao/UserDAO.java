package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}

