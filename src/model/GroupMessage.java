package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * グループチャット投稿
 */
public class GroupMessage {
    private int id;
    private int groupId;
    private int userId;
    private String userName;
    private String userLoginId;
    private String content;
    private LocalDateTime createdAt;
    private List<GroupAttachment> attachments = new ArrayList<>();

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<GroupAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<GroupAttachment> attachments) {
        this.attachments = attachments;
    }
}


