package model;

/**
 * ユーザーモデルクラス
 */
public class User {
    private int id;
    private String loginId;
    private String passwordHash;
    private String name;
    
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
    
    public String getName() {
        return name;
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
    
    public void setName(String name) {
        this.name = name;
    }
}

