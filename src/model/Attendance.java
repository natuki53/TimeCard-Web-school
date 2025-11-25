package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 勤怠モデルクラス
 */
public class Attendance{
    private int id;
    private int userId;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    
    // デフォルトコンストラクタ
    public Attendance() {
    }
    
    // 全フィールドのコンストラクタ
    public Attendance(int id, int userId, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.userId = userId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // ゲッター
    public int getId() {
        return id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public LocalDate getWorkDate() {
        return workDate;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    // セッター
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}

