package model;

import java.time.LocalDateTime;

/**
 * グループ情報を表すモデルクラス
 */
public class Group {
    private int id;
    private String name;
    private String description;
    private int adminUserId;
    private boolean deleted;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // デフォルトコンストラクタ
    public Group() {}
    
    // 全フィールドコンストラクタ
    public Group(int id, String name, String description, int adminUserId, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.adminUserId = adminUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // 新規作成用コンストラクタ（IDなし）
    public Group(String name, String description, int adminUserId) {
        this.name = name;
        this.description = description;
        this.adminUserId = adminUserId;
    }
    
    // Getter/Setter
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getAdminUserId() {
        return adminUserId;
    }
    
    public void setAdminUserId(int adminUserId) {
        this.adminUserId = adminUserId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", adminUserId=" + adminUserId +
                ", deleted=" + deleted +
                ", deletedAt=" + deletedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
