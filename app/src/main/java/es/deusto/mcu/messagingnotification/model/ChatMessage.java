package es.deusto.mcu.messagingnotification.model;

public class ChatMessage {
    private int conversationId;
    private String title;
    private String sender;
    private String msg;

    public ChatMessage(int conversationId, String title, String sender, String msg) {
        this.conversationId = conversationId;
        this.title = title;
        this.sender = sender;
        this.msg = msg;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
