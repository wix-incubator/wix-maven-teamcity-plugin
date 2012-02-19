package com.wixpress.ci.teamcity.domain;

/**
* @author yoav
* @since 2/19/12
*/
public class LogMessageException {
    private String exceptionClass;
    private String exceptionMessage;
    private LogMessageException cause;

    public LogMessageException() {
    }

    public LogMessageException(Throwable exception) {
        this.exceptionClass = exception.getClass().getName();
        this.exceptionMessage = exception.getMessage();
        if (exception.getCause() != null && exception.getCause() != exception)
            cause = new LogMessageException(exception.getCause());
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public LogMessageException getCause() {
        return cause;
    }

    public void setCause(LogMessageException cause) {
        this.cause = cause;
    }
}
