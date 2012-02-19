package com.wixpress.ci.teamcity.mavenAnalyzer;

import com.wixpress.ci.teamcity.domain.LogMessage;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
* @author yoav
* @since 2/19/12
*/
public class CollectProgress {
    private List<LogMessage> messages;
    private int position;
    private boolean completed;
    private boolean runFound;
    private String buildTypeId;

    public CollectProgress() {
    }

    public CollectProgress(List<LogMessage> messages, boolean completed, String buildTypeId) {
        this.messages = messages;
        this.position = messages.size();
        this.completed = completed;
        this.runFound = true;
        this.buildTypeId = buildTypeId;
    }

    public CollectProgress(List<LogMessage> messages, int position, boolean completed, String buildTypeId) {
        this.messages = messages;
        this.position = position;
        this.completed = completed;
        this.runFound = true;
        this.buildTypeId = buildTypeId;
    }

    public CollectProgress(String buildTypeId) {
        this.messages = newArrayList();
        this.position = 0;
        this.completed = false;
        this.runFound = false;
        this.buildTypeId = buildTypeId;
    }

    public List<LogMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<LogMessage> messages) {
        this.messages = messages;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isRunFound() {
        return runFound;
    }

    public void setRunFound(boolean runFound) {
        this.runFound = runFound;
    }

    public String getBuildTypeId() {
        return buildTypeId;
    }

    public void setBuildTypeId(String buildTypeId) {
        this.buildTypeId = buildTypeId;
    }
}
