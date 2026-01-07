package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import model.Attendance;
import model.AttendanceBreak;

import util.DBUtil;

/**
 * 勤怠DAOクラス（データベースアクセス）
 */
public class AttendanceDAO {
    
    /**
     * ユーザーID・日付・グループIDで勤怠を検索
     * @param userId ユーザーID
     * @param date 日付
     * @param groupId グループID（null はグループなし）
     * @return 見つかった勤怠、見つからなければnull
     */
    public Attendance findByUserIdAndDate(int userId, Date date, Integer groupId) {
        // JDBCドライバをロード
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			throw new IllegalStateException("JDBCドライバを読み込めませんでした");
		}
        
        // データベースに接続して勤怠検索を実行
		try(Connection conn = DBUtil.getConnection()){
            // ユーザーIDと日付（＋グループID）で勤怠を検索するSQL文
            final String sql;
            if (groupId == null) {
                sql = "SELECT id, user_id, group_id, work_date, start_time, end_time, prev_start_time, prev_end_time, "
                    + "is_cancelled, cancelled_by_admin, "
                    + "is_corrected, corrected_by_admin "
                    + "FROM attendance WHERE user_id = ? AND work_date = ? AND group_id IS NULL";
            } else {
                sql = "SELECT id, user_id, group_id, work_date, start_time, end_time, prev_start_time, prev_end_time, "
                    + "is_cancelled, cancelled_by_admin, "
                    + "is_corrected, corrected_by_admin "
                    + "FROM attendance WHERE user_id = ? AND work_date = ? AND group_id = ?";
            }
			PreparedStatement ps = conn.prepareStatement(sql);
            
			// SQL文のプレースホルダーに値を設定
			ps.setInt(1, userId);
			ps.setDate(2, date);
            if (groupId != null) {
                ps.setInt(3, groupId);
            }
			
			// SQL文を実行して勤怠情報を取得
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
                // 検索結果からAttendanceオブジェクトを作成
				Attendance attendance = new Attendance();
				attendance.setId(rs.getInt("id"));
				attendance.setUserId(rs.getInt("user_id"));
                attendance.setGroupId((Integer) rs.getObject("group_id"));
				attendance.setWorkDate(rs.getDate("work_date").toLocalDate());
                // 開始時刻がnullでない場合のみ設定
				if (rs.getTime("start_time") != null) {
					attendance.setStartTime(rs.getTime("start_time").toLocalTime());
				}
                // 終了時刻がnullでない場合のみ設定
				if (rs.getTime("end_time") != null) {
					attendance.setEndTime(rs.getTime("end_time").toLocalTime());
				}
                if (rs.getTime("prev_start_time") != null) {
                    attendance.setPrevStartTime(rs.getTime("prev_start_time").toLocalTime());
                }
                if (rs.getTime("prev_end_time") != null) {
                    attendance.setPrevEndTime(rs.getTime("prev_end_time").toLocalTime());
                }
                attendance.setCancelled(rs.getInt("is_cancelled") == 1);
                attendance.setCancelledByAdmin(rs.getInt("cancelled_by_admin") == 1);
                attendance.setCorrected(rs.getInt("is_corrected") == 1);
                attendance.setCorrectedByAdmin(rs.getInt("corrected_by_admin") == 1);
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
     * ユーザーIDと日付で勤怠を検索（グループなし）
     * 既存コード互換用
     */
    public Attendance findByUserIdAndDate(int userId, Date date) {
        return findByUserIdAndDate(userId, date, null);
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
		try(Connection conn = DBUtil.getConnection()){
            // 新規勤怠を登録するSQL文（出勤時刻のみ登録）
            final String sql;
            if (attendance.getGroupId() == null) {
                sql = "INSERT INTO attendance(user_id, group_id, work_date, start_time) VALUES(?, NULL, ?, ?)";
            } else {
                sql = "INSERT INTO attendance(user_id, group_id, work_date, start_time) VALUES(?, ?, ?, ?)";
            }
			PreparedStatement ps = conn.prepareStatement(sql);
			
            // SQL文のプレースホルダーに値を設定
			ps.setInt(1, attendance.getUserId());
            int idx = 2;
            if (attendance.getGroupId() != null) {
                ps.setInt(idx++, attendance.getGroupId());
            }
			ps.setDate(idx++, Date.valueOf(attendance.getWorkDate()));
			ps.setTime(idx, java.sql.Time.valueOf(attendance.getStartTime()));
			
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
     * 新規勤怠を「修正」として登録（出勤/退勤ともに登録可能）
     * 既存レコードが無い日の修正に使用する
     */
    public boolean insertAsCorrection(Attendance attendance, boolean correctedByAdmin, int correctedByUserId) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            final String sql;
            if (attendance.getGroupId() == null) {
                sql = "INSERT INTO attendance(user_id, group_id, work_date, start_time, end_time, prev_start_time, prev_end_time, is_corrected, corrected_by_admin, corrected_by_user_id, corrected_at) "
                    + "VALUES(?, NULL, ?, ?, ?, NULL, NULL, 1, ?, ?, NOW())";
            } else {
                sql = "INSERT INTO attendance(user_id, group_id, work_date, start_time, end_time, prev_start_time, prev_end_time, is_corrected, corrected_by_admin, corrected_by_user_id, corrected_at) "
                    + "VALUES(?, ?, ?, ?, ?, NULL, NULL, 1, ?, ?, NOW())";
            }
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, attendance.getUserId());
            int idx = 2;
            if (attendance.getGroupId() != null) {
                ps.setInt(idx++, attendance.getGroupId());
            }
            ps.setDate(idx++, Date.valueOf(attendance.getWorkDate()));
            ps.setTime(idx++, attendance.getStartTime() != null ? java.sql.Time.valueOf(attendance.getStartTime()) : null);
            ps.setTime(idx++, attendance.getEndTime() != null ? java.sql.Time.valueOf(attendance.getEndTime()) : null);
            ps.setInt(idx++, correctedByAdmin ? 1 : 0);
            ps.setInt(idx, correctedByUserId);

            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
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
        // JDBCドライバをロード
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			throw new IllegalStateException("JDBCドライバを読み込めませんでした");
		}
        
        // データベースに接続して勤怠更新を実行
		try(Connection conn = DBUtil.getConnection()){
            // 出勤時刻と退勤時刻を更新するSQL文
			String sql = "UPDATE attendance SET start_time = ?, end_time = ? WHERE id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			
            // SQL文のプレースホルダーに値を設定
			ps.setTime(1, attendance.getStartTime() != null ? java.sql.Time.valueOf(attendance.getStartTime()) : null);
			ps.setTime(2, attendance.getEndTime() != null ? java.sql.Time.valueOf(attendance.getEndTime()) : null);
			ps.setInt(3, attendance.getId());
			
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
     * 勤怠修正として更新（修正フラグ/修正者種別も更新）
     * @param attendance 更新対象（id が必要）
     * @param correctedByAdmin 管理者修正なら true
     * @param correctedByUserId 修正を行ったユーザーID
     */
    public boolean updateAsCorrection(Attendance attendance, boolean correctedByAdmin, int correctedByUserId) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            // 修正前の値を prev_* に退避してから上書き
            String sql = "UPDATE attendance SET "
                       + "prev_start_time = start_time, prev_end_time = end_time, "
                       + "start_time = ?, end_time = ?, "
                       + "is_corrected = 1, corrected_by_admin = ?, corrected_by_user_id = ?, corrected_at = NOW() "
                       + "WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTime(1, attendance.getStartTime() != null ? java.sql.Time.valueOf(attendance.getStartTime()) : null);
            ps.setTime(2, attendance.getEndTime() != null ? java.sql.Time.valueOf(attendance.getEndTime()) : null);
            ps.setInt(3, correctedByAdmin ? 1 : 0);
            ps.setInt(4, correctedByUserId);
            ps.setInt(5, attendance.getId());
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 休憩中かどうか（終了していない休憩レコードが存在するか）
     */
    public boolean hasOpenBreak(int attendanceId) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT COUNT(*) FROM attendance_breaks WHERE attendance_id = ? AND break_end IS NULL";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 休憩開始（複数回OK）
     * - 休憩中（open break がある）場合は開始しない
     */
    public boolean startBreak(int attendanceId, LocalTime breakStart) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            // 既に休憩中なら開始しない
            if (hasOpenBreak(attendanceId)) return false;

            String sql = "INSERT INTO attendance_breaks(attendance_id, break_start) VALUES(?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, attendanceId);
            ps.setTime(2, java.sql.Time.valueOf(breakStart));
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 休憩終了（最後の open break を閉じる）
     */
    public boolean endBreak(int attendanceId, LocalTime breakEnd) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE attendance_breaks SET break_end = ? "
                       + "WHERE attendance_id = ? AND break_end IS NULL "
                       + "ORDER BY id DESC LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTime(1, java.sql.Time.valueOf(breakEnd));
            ps.setInt(2, attendanceId);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 現在休憩中の場合、開いている休憩の開始時刻を取得（なければnull）
     */
    public LocalTime findOpenBreakStart(int attendanceId) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT break_start FROM attendance_breaks "
                       + "WHERE attendance_id = ? AND break_end IS NULL "
                       + "ORDER BY id DESC LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getTime(1) != null) {
                    return rs.getTime(1).toLocalTime();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 休憩一覧を取得（開始順）
     */
    public List<AttendanceBreak> findBreaksByAttendanceId(int attendanceId) {
        List<AttendanceBreak> breaks = new ArrayList<>();

        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT id, attendance_id, break_start, break_end "
                       + "FROM attendance_breaks WHERE attendance_id = ? ORDER BY id ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceBreak br = new AttendanceBreak();
                    br.setId(rs.getInt("id"));
                    br.setAttendanceId(rs.getInt("attendance_id"));
                    br.setBreakStart(rs.getTime("break_start").toLocalTime());
                    br.setBreakEnd(rs.getTime("break_end") != null ? rs.getTime("break_end").toLocalTime() : null);
                    breaks.add(br);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return breaks;
    }

    /**
     * 休憩合計（分）を取得（終了している休憩のみ）
     */
    public int sumBreakMinutes(int attendanceId) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE, "
                       + "  TIMESTAMP(CURDATE(), break_start), "
                       + "  TIMESTAMP(CURDATE(), break_end)"
                       + ")), 0) "
                       + "FROM attendance_breaks "
                       + "WHERE attendance_id = ? AND break_end IS NOT NULL";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 分を HH:mm に整形
     */
    public static String formatMinutesHHmm(int minutes) {
        if (minutes <= 0) return "00:00";
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format("%02d:%02d", h, m);
    }

    /**
     * 勤怠取消（出勤取り消し）
     * - 時刻はNULLに戻し、取消フラグを立てる
     */
    public boolean cancelAttendance(int attendanceId, boolean cancelledByAdmin, int cancelledByUserId) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE attendance SET start_time = NULL, end_time = NULL, "
                       + "is_cancelled = 1, cancelled_by_admin = ?, cancelled_by_user_id = ?, cancelled_at = NOW() "
                       + "WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cancelledByAdmin ? 1 : 0);
            ps.setInt(2, cancelledByUserId);
            ps.setInt(3, attendanceId);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 取消状態を解除（再打刻用）
     */
    public boolean clearCancellation(int attendanceId) {
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE attendance SET is_cancelled = 0, cancelled_by_admin = 0, "
                       + "cancelled_by_user_id = NULL, cancelled_at = NULL WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, attendanceId);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    /**
     * ユーザーIDと日付で勤怠を検索（LocalDate版）
     * @param userId ユーザーID
     * @param date 日付
     * @return 見つかった勤怠、見つからなければnull
     */
    public Attendance findByUserIdAndDate(int userId, LocalDate date) {
        return findByUserIdAndDate(userId, Date.valueOf(date), null);
    }

    /**
     * ユーザーIDと日付で勤怠を検索（LocalDate + groupId版）
     */
    public Attendance findByUserIdAndDate(int userId, LocalDate date, Integer groupId) {
        return findByUserIdAndDate(userId, Date.valueOf(date), groupId);
    }
    
    /**
     * ユーザーの最近の勤怠履歴を取得
     * @param userId ユーザーID
     * @param limit 取得件数
     * @return 勤怠履歴リスト
     */
    public List<Attendance> findRecentByUserId(int userId, int limit) {
        List<Attendance> attendanceList = new ArrayList<>();
        
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        
        // データベースに接続して勤怠履歴を取得
        try(Connection conn = DBUtil.getConnection()) {
            // 最近の勤怠履歴を取得するSQL文（日付降順）
            // - group_id はNULLの場合があるため LEFT JOIN
            // - 表示用に group_name を取得（NULLなら「グループなし」）
            String sql = "SELECT a.id, a.user_id, a.group_id, "
                        + "COALESCE(g.name, 'グループなし') AS group_name, "
                        + "a.work_date, a.start_time, a.end_time, a.prev_start_time, a.prev_end_time, "
                        + "a.is_cancelled, a.cancelled_by_admin, "
                        + "a.is_corrected, a.corrected_by_admin "
                        + "FROM attendance a "
                        + "LEFT JOIN `groups` g ON a.group_id = g.id AND g.is_deleted = 0 "
                        + "WHERE a.user_id = ? "
                        + "ORDER BY a.work_date DESC, a.id DESC LIMIT ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // SQL文のプレースホルダーに値を設定
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            
            // SQL文を実行して勤怠履歴を取得
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                // 検索結果からAttendanceオブジェクトを作成
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setUserId(rs.getInt("user_id"));
                attendance.setGroupId((Integer) rs.getObject("group_id"));
                attendance.setGroupName(rs.getString("group_name"));
                attendance.setWorkDate(rs.getDate("work_date").toLocalDate());
                attendance.setStartTime(rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null);
                attendance.setEndTime(rs.getTime("end_time") != null ? rs.getTime("end_time").toLocalTime() : null);
                attendance.setPrevStartTime(rs.getTime("prev_start_time") != null ? rs.getTime("prev_start_time").toLocalTime() : null);
                attendance.setPrevEndTime(rs.getTime("prev_end_time") != null ? rs.getTime("prev_end_time").toLocalTime() : null);
                attendance.setCancelled(rs.getInt("is_cancelled") == 1);
                attendance.setCancelledByAdmin(rs.getInt("cancelled_by_admin") == 1);
                attendance.setCorrected(rs.getInt("is_corrected") == 1);
                attendance.setCorrectedByAdmin(rs.getInt("corrected_by_admin") == 1);
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            // データベースエラーをコンソールに出力
            e.printStackTrace();
        }
        
        return attendanceList;
    }

    /**
     * ユーザーの指定月の勤怠履歴を取得（全グループ混在）
     * - group_id=NULL も含む
     * - 表示用に group_name を付与
     * @param userId ユーザーID
     * @param year 年
     * @param month 月
     * @param limit 最大件数（0以下なら無制限）
     */
    public List<Attendance> findByUserIdAndMonthAllGroups(int userId, int year, int month, int limit) {
        List<Attendance> attendanceList = new ArrayList<>();

        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT a.id, a.user_id, a.group_id, "
                       + "COALESCE(g.name, 'グループなし') AS group_name, "
                       + "a.work_date, a.start_time, a.end_time, a.prev_start_time, a.prev_end_time, "
                       + "a.is_cancelled, a.cancelled_by_admin, "
                       + "a.is_corrected, a.corrected_by_admin "
                       + "FROM attendance a "
                       + "LEFT JOIN `groups` g ON a.group_id = g.id AND g.is_deleted = 0 "
                       + "WHERE a.user_id = ? AND YEAR(a.work_date) = ? AND MONTH(a.work_date) = ? "
                       + "ORDER BY a.work_date DESC, a.id DESC";
            if (limit > 0) {
                sql += " LIMIT ?";
            }

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            if (limit > 0) {
                ps.setInt(4, limit);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setUserId(rs.getInt("user_id"));
                attendance.setGroupId((Integer) rs.getObject("group_id"));
                attendance.setGroupName(rs.getString("group_name"));
                attendance.setWorkDate(rs.getDate("work_date").toLocalDate());
                attendance.setStartTime(rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null);
                attendance.setEndTime(rs.getTime("end_time") != null ? rs.getTime("end_time").toLocalTime() : null);
                attendance.setPrevStartTime(rs.getTime("prev_start_time") != null ? rs.getTime("prev_start_time").toLocalTime() : null);
                attendance.setPrevEndTime(rs.getTime("prev_end_time") != null ? rs.getTime("prev_end_time").toLocalTime() : null);
                attendance.setCancelled(rs.getInt("is_cancelled") == 1);
                attendance.setCancelledByAdmin(rs.getInt("cancelled_by_admin") == 1);
                attendance.setCorrected(rs.getInt("is_corrected") == 1);
                attendance.setCorrectedByAdmin(rs.getInt("corrected_by_admin") == 1);
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }
    
    /**
     * ユーザーの指定月の勤怠履歴を取得
     * @param userId ユーザーID
     * @param year 年
     * @param month 月
     * @return 勤怠履歴リスト
     */
    public List<Attendance> findByUserIdAndMonth(int userId, int year, int month) {
        return findByUserIdAndMonth(userId, null, year, month);
    }

    /**
     * ユーザーの指定月の勤怠履歴を取得（グループ別）
     * @param userId ユーザーID
     * @param groupId グループID（nullはグループなし）
     * @param year 年
     * @param month 月
     */
    public List<Attendance> findByUserIdAndMonth(int userId, Integer groupId, int year, int month) {
        List<Attendance> attendanceList = new ArrayList<>();
        
        // JDBCドライバをロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("JDBCドライバを読み込めませんでした");
        }
        
        // データベースに接続して勤怠履歴を取得
        try(Connection conn = DBUtil.getConnection()) {
            // 指定月の勤怠履歴を取得するSQL文（グループ別）
            final String sql;
            if (groupId == null) {
                sql = "SELECT id, user_id, group_id, work_date, start_time, end_time, prev_start_time, prev_end_time, "
                    + "is_cancelled, cancelled_by_admin, "
                    + "is_corrected, corrected_by_admin "
                    + "FROM attendance WHERE user_id = ? AND group_id IS NULL "
                    + "AND YEAR(work_date) = ? AND MONTH(work_date) = ? "
                    + "ORDER BY work_date ASC";
            } else {
                sql = "SELECT id, user_id, group_id, work_date, start_time, end_time, prev_start_time, prev_end_time, "
                    + "is_cancelled, cancelled_by_admin, "
                    + "is_corrected, corrected_by_admin "
                    + "FROM attendance WHERE user_id = ? AND group_id = ? "
                    + "AND YEAR(work_date) = ? AND MONTH(work_date) = ? "
                    + "ORDER BY work_date ASC";
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // SQL文のプレースホルダーに値を設定
            ps.setInt(1, userId);
            int idx = 2;
            if (groupId != null) {
                ps.setInt(idx++, groupId);
            }
            ps.setInt(idx++, year);
            ps.setInt(idx, month);
            
            // SQL文を実行して勤怠履歴を取得
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                // 検索結果からAttendanceオブジェクトを作成
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setUserId(rs.getInt("user_id"));
                attendance.setGroupId((Integer) rs.getObject("group_id"));
                attendance.setWorkDate(rs.getDate("work_date").toLocalDate());
                attendance.setStartTime(rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null);
                attendance.setEndTime(rs.getTime("end_time") != null ? rs.getTime("end_time").toLocalTime() : null);
                attendance.setPrevStartTime(rs.getTime("prev_start_time") != null ? rs.getTime("prev_start_time").toLocalTime() : null);
                attendance.setPrevEndTime(rs.getTime("prev_end_time") != null ? rs.getTime("prev_end_time").toLocalTime() : null);
                attendance.setCancelled(rs.getInt("is_cancelled") == 1);
                attendance.setCancelledByAdmin(rs.getInt("cancelled_by_admin") == 1);
                attendance.setCorrected(rs.getInt("is_corrected") == 1);
                attendance.setCorrectedByAdmin(rs.getInt("corrected_by_admin") == 1);
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            // データベースエラーをコンソールに出力
            e.printStackTrace();
        }
        
        return attendanceList;
    }

    /**
     * 文字列(HH:mm)を LocalTime に変換（空やnullはnull）
     */
    public static LocalTime parseTimeOrNull(String hhmm) {
        if (hhmm == null) return null;
        String v = hhmm.trim();
        if (v.isEmpty()) return null;
        return LocalTime.parse(v);
    }
}

