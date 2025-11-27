package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import model.User;

/**
 * ユーザーDAOクラス（データベースアクセス）
 */
public class UserDAO {
	private final String JDBC_URL = "jdbc:mysql://localhost:3306/timecard_db?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8";
	private final String DB_USER = "root";
	private final String DB_PASS = "root";
    /**
     * ログインIDとパスワードでユーザーを検索
     * @param loginId ログインID
     * @param password パスワード
     * @return 見つかったユーザー、見つからなければnull
     */
    public User findByLoginIdAndPassword(String loginId, String password) {
        // TODO: 実装
    	try {
			Class.forName("com.mysql.jdbc.Driver");
			try(Connection conn = DriverManager.getConnection(JDBC_URL,DB_USER,DB_PASS)){
				String sql = "SELECT id, login_id, password_hash, name FROM users WHERE login_id = ? AND password_hash = ?";
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, loginId);
				ps.setString(2, password);
				ResultSet rs = ps.executeQuery();
				
				if(rs.next()) {
					User user = new User();
					user.setId(rs.getInt("id"));
					user.setLoginId(rs.getString("login_id"));
					user.setPasswordHash(rs.getString("password_hash"));
					user.setName(rs.getString("name"));
					return user;
				}

			}
		}catch(Exception e) {
			e.printStackTrace();
			
		}
        return null;
    }
    
    /**
     * 新規ユーザーを登録
     * @param user 登録するユーザー
     * @return 登録成功したらtrue
     */
    public boolean insert(User user) {
        // TODO: 実装
    	try {
			Class.forName("com.mysql.jdbc.Driver");
			try(Connection conn = DriverManager.getConnection(JDBC_URL,DB_USER,DB_PASS)){
				String sql = "SELECT login_id FROM USERS WHERE login_id = ?";
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, user.getLoginId());
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					return false;
				}
				
				String sql2 = "INSERT INTO users(login_id, password_hash, name) VALUES (?, ?, ?)";
				ps = conn.prepareStatement(sql2);
				ps.setString(1, user.getLoginId());
				ps.setString(2, user.getPasswordHash());
				ps.setString(3, user.getName());
				
				int result = ps.executeUpdate();
				if(result != 1) {
					return true;
				}
				
			}
			}catch(Exception e) {
				e.printStackTrace();
			}
        return false;
    }
}

