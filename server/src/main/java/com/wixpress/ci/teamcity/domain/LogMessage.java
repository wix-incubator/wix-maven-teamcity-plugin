package com.wixpress.ci.teamcity.domain;

/**
* @author yoav
* @since 2/19/12
*/
public class LogMessage {
    private String messsage;
    private LogMessageType messageType;
    private LogMessageException exception;

    public LogMessage() {
    }

    public LogMessage(String message, LogMessageType messageType) {
        this.messsage = message;
        this.messageType = messageType;
    }

    public LogMessage(String message, LogMessageType messageType, Exception exception) {
        this.messsage = message;
        this.messageType = messageType;
        this.exception = new LogMessageException(exception);
    }

    public String getMessage() {
        return messsage;
    }

    public void setMessage(String messsage) {
        this.messsage = messsage;
    }

    public LogMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(LogMessageType messageType) {
        this.messageType = messageType;
    }

    public LogMessageException getException() {
        return exception;
    }

    public void setException(LogMessageException exception) {
        this.exception = exception;
    }
}
