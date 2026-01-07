package model;

import java.time.LocalDateTime;

/**
 * DMスレッド（相手ユーザー情報つき表示用）
 */
public class DmThread {
    private int id;
    private int otherUserId;
    private String otherLoginId;
    private String otherName;
    private String otherIconFilename;
    private String lastContent;
    private LocalDateTime lastCreatedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOtherUserId() { return otherUserId; }
    public void setOtherUserId(int otherUserId) { this.otherUserId = otherUserId; }

    public String getOtherLoginId() { return otherLoginId; }
    public void setOtherLoginId(String otherLoginId) { this.otherLoginId = otherLoginId; }

    public String getOtherName() { return otherName; }
    public void setOtherName(String otherName) { this.otherName = otherName; }

    public String getOtherIconFilename() { return otherIconFilename; }
    public void setOtherIconFilename(String otherIconFilename) { this.otherIconFilename = otherIconFilename; }

    public String getLastContent() { return lastContent; }
    public void setLastContent(String lastContent) { this.lastContent = lastContent; }

    public LocalDateTime getLastCreatedAt() { return lastCreatedAt; }
    public void setLastCreatedAt(LocalDateTime lastCreatedAt) { this.lastCreatedAt = lastCreatedAt; }
}


