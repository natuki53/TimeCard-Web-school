package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 勤怠モデルクラス
 */
public class Attendance{
    private int id;
    private int userId;
    /**
     * グループID（未所属/グループなしの場合は null）
     */
    private Integer groupId;
    /**
     * 表示用のグループ名（group_id が null の場合は「グループなし」を想定）
     * DBには保持しない。
     */
    private String groupName;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime prevStartTime;
    private LocalTime prevEndTime;

    /**
     * 修正情報
     * - isCorrected: 修正済みか
     * - correctedByAdmin: 管理者修正ならtrue、一般ユーザー修正ならfalse
     */
    private boolean isCorrected;
    private boolean correctedByAdmin;

    /**
     * 取消情報（出勤取り消し）
     * - isCancelled: 取消済みか
     * - cancelledByAdmin: 管理者取消ならtrue、一般ユーザー取消ならfalse
     */
    private boolean isCancelled;
    private boolean cancelledByAdmin;
    
    // デフォルトコンストラクタ
    public Attendance() {
    }
    
    // 全フィールドのコンストラクタ
    public Attendance(int id, int userId, Integer groupId, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.userId = userId;
        this.groupId = groupId;
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

    public Integer getGroupId() {
        return groupId;
    }
    
    public String getGroupName() {
        return groupName;
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

    public LocalTime getPrevStartTime() {
        return prevStartTime;
    }

    public LocalTime getPrevEndTime() {
        return prevEndTime;
    }

    public boolean isCorrected() {
        return isCorrected;
    }

    public boolean isCorrectedByAdmin() {
        return correctedByAdmin;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isCancelledByAdmin() {
        return cancelledByAdmin;
    }
    
    // セッター
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public void setPrevStartTime(LocalTime prevStartTime) {
        this.prevStartTime = prevStartTime;
    }

    public void setPrevEndTime(LocalTime prevEndTime) {
        this.prevEndTime = prevEndTime;
    }

    public void setCorrected(boolean corrected) {
        isCorrected = corrected;
    }

    public void setCorrectedByAdmin(boolean correctedByAdmin) {
        this.correctedByAdmin = correctedByAdmin;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public void setCancelledByAdmin(boolean cancelledByAdmin) {
        this.cancelledByAdmin = cancelledByAdmin;
    }
}

