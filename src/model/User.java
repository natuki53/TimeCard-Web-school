package model;

import java.time.LocalDateTime;

/**
 * ユーザーモデルクラス
 */
public class User {
    private int id;
    private String loginId;
    private String passwordHash;
    private String secretQuestion;
    private String secretAnswerHash;
    private String name;
    private String bio;
    private boolean dmAllowed = true;
    private String iconFilename;
    private boolean deleted;
    private LocalDateTime deletedAt;
    
    // デフォルトコンストラクタ
    public User() {
    }
    
    // 全フィールドのコンストラクタ
    public User(int id, String loginId, String passwordHash, String name) {
        this.id = id;
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
    }
    
    // ゲッター
    public int getId() {
        return id;
    }
    
    public String getLoginId() {
        return loginId;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public String getSecretQuestion() {
        return secretQuestion;
    }

    public String getSecretAnswerHash() {
        return secretAnswerHash;
    }
    
    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }

    public boolean isDmAllowed() {
        return dmAllowed;
    }

    public String getIconFilename() {
        return iconFilename;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    // セッター
    public void setId(int id) {
        this.id = id;
    }
    
    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setSecretQuestion(String secretQuestion) {
        this.secretQuestion = secretQuestion;
    }

    public void setSecretAnswerHash(String secretAnswerHash) {
        this.secretAnswerHash = secretAnswerHash;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setDmAllowed(boolean dmAllowed) {
        this.dmAllowed = dmAllowed;
    }

    public void setIconFilename(String iconFilename) {
        this.iconFilename = iconFilename;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}

