package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import model.Attendance;

/**
 * 勤怠DAOクラス（データベースアクセス）
 */
public class AttendanceDAO {
    
	private final String JDBC_URL = "jdbc:mysql://localhost:3306/timecard_db?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8";
	private final String DB_USER = "root";
	private final String DB_PASS = "root";
    /**
     * ユーザーIDと日付で勤怠を検索
     * @param userId ユーザーID
     * @param date 日付
     * @return 見つかった勤怠、見つからなければnull
     */
    public Attendance findByUserIdAndDate(int userId, Date date) {
        // TODO: 実装
    	try {
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			throw new IllegalStateException("JDBCドライバを読み込めませんでした");
		}
		try(Connection conn = DriverManager.getConnection(JDBC_URL,DB_USER,DB_PASS)){
			String sql = "SELECT id, user_id, work_date, start_time, end_time FROM attendance WHERE user_id = ? AND work_date = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			//sql文の?に入れるものを定義
			ps.setInt(1, userId);
			ps.setDate(2, date);
			
			//sql文で見つかったidを返す
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				Attendance attendance = new Attendance();
				attendance.setId(rs.getInt("id"));
				attendance.setUserId(rs.getInt("user_id"));
				attendance.setWorkDate(rs.getDate("work_date").toLocalDate());
				if(rs.getTime("start_time") != null) {
					attendance.setStartTime(rs.getTime("start_time").toLocalTime());
				}
				if(rs.getTime("end_time") != null) {
					attendance.setEndTime(rs.getTime("end_time").toLocalTime());
				}
				return attendance;
			}
		}catch (SQLException e) {
			e.printStackTrace();
			
		}
		
        return null;
    }
    
    /**
     * 新規勤怠を登録
     * @param attendance 登録する勤怠
     * @return 登録成功したらtrue
     */
    public boolean insert(Attendance attendance) {
        // TODO: 実装
    	try {
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			throw new IllegalStateException("JDBCドライバを読み込めませんでした");
		}
		try(Connection conn = DriverManager.getConnection(JDBC_URL,DB_USER,DB_PASS)){
			String sql = "INSERT INTO attendance(user_id, work_date, start_time) VALUES(?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			
			ps.setInt(1, attendance.getUserId());
			ps.setDate(2, Date.valueOf(attendance.getWorkDate()));
			ps.setTime(3, java.sql.Time.valueOf(attendance.getStartTime()));
			
			int result = ps.executeUpdate();
			if(result > 0) {
				return true;
			}
			
		}catch (SQLException e) {
			e.printStackTrace();
			
		}
        return false;
    }
    
    /**
     * 勤怠を更新
     * @param attendance 更新する勤怠
     * @return 更新成功したらtrue
     */
    public boolean update(Attendance attendance) {
        // TODO: 実装
        return false;
    }
    
    /**
     * ユーザーIDと年月で勤怠一覧を取得
     * @param userId ユーザーID
     * @param year 年
     * @param month 月
     * @return 勤怠一覧
     */
    public List<Attendance> findByUserIdAndMonth(int userId, int year, int month) {
        // TODO: 実装
        return null;
    }
}

