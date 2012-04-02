package com.wixpress.ci.teamcity.mavenAnalyzer.dao;

import com.wixpress.ci.teamcity.domain.LogMessage;
import com.wixpress.ci.teamcity.domain.MModule;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
* @author yoav
* @since 2/23/12
*/
public class ModuleDependenciesStorage {
    private MModule module;
    private Map<String, String> vcsRevisions = newHashMap();
    private List<LogMessage> messages = newArrayList();
    private boolean isException;

    public ModuleDependenciesStorage() {
    }

    public ModuleDependenciesStorage(MModule module, Map<String, String> vcsRevisions) {
        this.module = module;
        this.vcsRevisions = vcsRevisions;
        this.isException = false;
    }

    public ModuleDependenciesStorage(List<LogMessage> messages) {
        this.messages = messages;
        this.isException = true;
    }

    public MModule getModule() {
        return module;
    }

    public void setModule(MModule module) {
        this.module = module;
    }

    public Map<String, String> getVcsRevisions() {
        return vcsRevisions;
    }

    public void setVcsRevisions(Map<String, String> vcsRevisions) {
        this.vcsRevisions = vcsRevisions;
    }

    public List<LogMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<LogMessage> messages) {
        this.messages = messages;
    }

    public boolean isException() {
        return isException;
    }

    public void setException(boolean exception) {
        isException = exception;
    }
}
