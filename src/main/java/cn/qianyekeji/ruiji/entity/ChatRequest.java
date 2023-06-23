package cn.qianyekeji.ruiji.entity;

/**
 * @author liangshuai
 * @date 2023/6/22
 */
public class ChatRequest {
    private String userId;
    private String message;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
