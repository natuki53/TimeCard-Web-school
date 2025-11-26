package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Attendance;

/**
 * 勤怠DAOクラス（データベースアクセス）
 */
public class AttendanceDAO {
    
    // データベース接続情報の定数定義
	private final String JDBC_URL = "jdbc:mysql://localhost:3306/timecard_db?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8";
	private final String DB_USER = "root";
	private final String DB_PASS = "";
    
    /**
     * ユーザーIDと日付で勤怠を検索
     * @param userId ユーザーID
     * @param date 日付
     * @return 見つかった勤怠、見つからなければnull
     */
    public Attendance findByUserIdAndDate(int userId, Date date) {
        // JDBCドライバをロード
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			throw new IllegalStateException("JDBCドライバを読み込めませんでした");
		}
        
        // データベースに接続して勤怠検索を実行
		try(Connection conn = DriverManager.getConnection(JDBC_URL,DB_USER,DB_PASS)){
            // ユーザーIDと日付で勤怠を検索するSQL文
			String sql = "SELECT id, user_id, work_date, start_time, end_time FROM attendance WHERE user_id = ? AND work_date = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
            
			// SQL文のプレースホルダーに値を設定
			ps.setInt(1, userId);
			ps.setDate(2, date);
			
			// SQL文を実行して勤怠情報を取得
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
                // 検索結果からAttendanceオブジェクトを作成
				Attendance attendance = new Attendance();
				attendance.setId(rs.getInt("id"));
				attendance.setUserId(rs.getInt("user_id"));
				attendance.setWorkDate(rs.getDate("work_date").toLocalDate());
                // 開始時刻がnullでない場合のみ設定
				if (rs.getTime("start_time") != null) {
					attendance.setStartTime(rs.getTime("start_time").toLocalTime());
				}
                // 終了時刻がnullでない場合のみ設定
				if (rs.getTime("end_time") != null) {
					attendance.setEndTime(rs.getTime("end_time").toLocalTime());
				}
				return attendance;
			}
		}catch (SQLException e) {
            // データベースエラーをコンソールに出力
			e.printStackTrace();
		}
		
        // 勤怠が見つからない場合はnullを返す
        return null;
    }
    
    /**
     * 新規勤怠を登録
     * @param attendance 登録する勤怠
     * @return 登録成功したらtrue
     */
    public boolean insert(Attendance attendance) {
        // JDBCドライバをロード
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			throw new IllegalStateException("JDBCドライバを読み込めませんでした");
		}
        
        // データベースに接続して勤怠登録を実行
		try(Connection conn = DriverManager.getConnection(JDBC_URL,DB_USER,DB_PASS)){
            // 新規勤怠を登録するSQL文（出勤時刻のみ登録）
			String sql = "INSERT INTO attendance(user_id, work_date, start_time) VALUES(?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			
            // SQL文のプレースホルダーに値を設定
			ps.setInt(1, attendance.getUserId());
			ps.setDate(2, Date.valueOf(attendance.getWorkDate()));
			ps.setTime(3, java.sql.Time.valueOf(attendance.getStartTime()));
			
            // SQL文を実行して登録処理を行う
			int result = ps.executeUpdate();
            // 1行以上更新されれば登録成功
			return result > 0;
		}catch (SQLException e) {
            // データベースエラーをコンソールに出力
			e.printStackTrace();
		}
        
        // 登録失敗の場合はfalseを返す
        return false;
    }
    
    /**
     * 勤怠を更新
     * @param attendance 更新する勤怠
     * @return 更新成功したらtrue
     */
    public boolean update(Attendance attendance) {
        // JDBCドライバをロード
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			throw new IllegalStateException("JDBCドライバを読み込めませんでした");
		}
        
        // データベースに接続して勤怠更新を実行
		try(Connection conn = DriverManager.getConnection(JDBC_URL,DB_USER,DB_PASS)){
            // 退勤時刻を更新するSQL文
			String sql = "UPDATE attendance SET end_time = ? WHERE id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			
            // SQL文のプレースホルダーに値を設定
			ps.setTime(1, java.sql.Time.valueOf(attendance.getEndTime()));
			ps.setInt(2, attendance.getId());
			
            // SQL文を実行して更新処理を行う
			int result = ps.executeUpdate();
            // 1行以上更新されれば更新成功
			return result > 0;
		}catch (SQLException e) {
            // データベースエラーをコンソールに出力
			e.printStackTrace();
		}
        
        // 更新失敗の場合はfalseを返す
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
        // 勤怠一覧を格納するリストを初期化
        List<Attendance> attendanceList = new ArrayList<>();
        
        // JDBCドライバをロード
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			throw new IllegalStateException("JDBCドライバを読み込めませんでした");
		}
        
        // データベースに接続して勤怠一覧取得を実行
		try(Connection conn = DriverManager.getConnection(JDBC_URL,DB_USER,DB_PASS)){
            // ユーザーIDと年月で勤怠一覧を取得するSQL文（日付順でソート）
			String sql = "SELECT id, user_id, work_date, start_time, end_time FROM attendance WHERE user_id = ? AND YEAR(work_date) = ? AND MONTH(work_date) = ? ORDER BY work_date";
			PreparedStatement ps = conn.prepareStatement(sql);
            
            // SQL文のプレースホルダーに値を設定
			ps.setInt(1, userId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			
            // SQL文を実行して勤怠一覧を取得
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
                // 各行の検索結果からAttendanceオブジェクトを作成
				Attendance attendance = new Attendance();
				attendance.setId(rs.getInt("id"));
				attendance.setUserId(rs.getInt("user_id"));
				attendance.setWorkDate(rs.getDate("work_date").toLocalDate());
                // 開始時刻がnullでない場合のみ設定
				if (rs.getTime("start_time") != null) {
					attendance.setStartTime(rs.getTime("start_time").toLocalTime());
				}
                // 終了時刻がnullでない場合のみ設定
				if (rs.getTime("end_time") != null) {
					attendance.setEndTime(rs.getTime("end_time").toLocalTime());
				}
                // リストに追加
				attendanceList.add(attendance);
			}
		}catch (SQLException e) {
            // データベースエラーをコンソールに出力
			e.printStackTrace();
		}
        
        // 勤怠一覧を返す（見つからない場合は空のリスト）
        return attendanceList;
    }
}

