package dao;

import model.Attendance;
import java.time.LocalDate;
import java.util.List;

/**
 * 勤怠DAOクラス（データベースアクセス）
 */
public class AttendanceDAO {
    
    /**
     * ユーザーIDと日付で勤怠を検索
     * @param userId ユーザーID
     * @param date 日付
     * @return 見つかった勤怠、見つからなければnull
     */
    public Attendance findByUserIdAndDate(int userId, LocalDate date) {
        // TODO: 実装
        return null;
    }
    
    /**
     * 新規勤怠を登録
     * @param attendance 登録する勤怠
     * @return 登録成功したらtrue
     */
    public boolean insert(Attendance attendance) {
        // TODO: 実装
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

