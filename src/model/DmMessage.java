package model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DMメッセージ
 */
public class DmMessage {
    private int id;
    private int threadId;
    private int senderUserId;
    private String senderName;
    private String senderLoginId;
    private String content;
    private LocalDateTime createdAt;
    private List<DmAttachment> attachments;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getThreadId() { return threadId; }
    public void setThreadId(int threadId) { this.threadId = threadId; }

    public int getSenderUserId() { return senderUserId; }
    public void setSenderUserId(int senderUserId) { this.senderUserId = senderUserId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderLoginId() { return senderLoginId; }
    public void setSenderLoginId(String senderLoginId) { this.senderLoginId = senderLoginId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<DmAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<DmAttachment> attachments) { this.attachments = attachments; }
}


