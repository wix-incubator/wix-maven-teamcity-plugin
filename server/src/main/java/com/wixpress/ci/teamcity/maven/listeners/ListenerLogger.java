package com.wixpress.ci.teamcity.maven.listeners;

/**
 * @author yoav
 * @since 2/15/12
 */
public interface ListenerLogger {
    public void info(String message);
    public void progress(String message);
    public void error(String message);
    public void error(String message, Exception e);
}
