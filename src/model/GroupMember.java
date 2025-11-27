package model;

import java.time.LocalDateTime;

/**
 * グループメンバー情報を表すモデルクラス
 */
public class GroupMember {
    private int id;
    private int groupId;
    private int userId;
    private LocalDateTime joinedAt;
    
    // ユーザー情報（JOIN用）
    private String userName;
    private String userLoginId;
    
    // デフォルトコンストラクタ
    public GroupMember() {}
    
    // 全フィールドコンストラクタ
    public GroupMember(int id, int groupId, int userId, LocalDateTime joinedAt) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }
    
    // 新規作成用コンストラクタ（IDなし）
    public GroupMember(int groupId, int userId) {
        this.groupId = groupId;
        this.userId = userId;
    }
    
    // ユーザー情報付きコンストラクタ（JOIN結果用）
    public GroupMember(int id, int groupId, int userId, LocalDateTime joinedAt, 
                      String userName, String userLoginId) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.joinedAt = joinedAt;
        this.userName = userName;
        this.userLoginId = userLoginId;
    }
    
    // Getter/Setter
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getGroupId() {
        return groupId;
    }
    
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserLoginId() {
        return userLoginId;
    }
    
    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }
    
    @Override
    public String toString() {
        return "GroupMember{" +
                "id=" + id +
                ", groupId=" + groupId +
                ", userId=" + userId +
                ", joinedAt=" + joinedAt +
                ", userName='" + userName + '\'' +
                ", userLoginId='" + userLoginId + '\'' +
                '}';
    }
}
