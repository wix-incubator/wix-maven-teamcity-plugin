package com.wixpress.ci.teamcity.dependenciesTab;

import com.wixpress.ci.teamcity.dependenciesTab.mavenAnalyzer.DependenciesResult;
import com.wixpress.ci.teamcity.dependenciesTab.mavenAnalyzer.TeamCityBuildMavenDependenciesAnalyzer;
import com.wixpress.ci.teamcity.domain.LogMessage;
import com.wixpress.ci.teamcity.domain.LogMessageType;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab;
import org.codehaus.jackson.map.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/16/12
 */
public class DependenciesTabExtension extends BuildTypeTab {

    private TeamCityBuildMavenDependenciesAnalyzer dependenciesAnalyzer;
    private final ObjectMapper objectMapper;

    public DependenciesTabExtension(WebControllerManager manager, ProjectManager projectManager, TeamCityBuildMavenDependenciesAnalyzer dependenciesAnalyzer, ObjectMapper objectMapper) {
        super("wix-maven-3-teamcity-plugin", "Maven 3 Dependencies", manager, projectManager, "dependenciesTab.jsp");
        this.dependenciesAnalyzer = dependenciesAnalyzer;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void fillModel(Map model, HttpServletRequest request, @NotNull SBuildType buildType, SUser user) {
        try {
        DependenciesResult dependenciesResult = dependenciesAnalyzer.analyzeDependencies(buildType);
        switch (dependenciesResult.getResultType()) {
            case current:
            case needsRefresh:
                model.put("module", objectMapper.writeValueAsString(dependenciesResult.getModule()));
                model.put("resultType", "dependencies");
                break;
            case exception:
                model.put("fullTrace", objectMapper.writeValueAsString(dependenciesResult.getFullTrace()));
                model.put("resultType", "error");
                break;
            case notRun:
                model.put("resultType", "notRun");
                break;
            case runningAsync:
                model.put("resultType", "running");
        }
        } catch (Exception e) {
            model.put("fullTrace", serializeException(e));
            model.put("resultType", "error");
        }
    }
    
    private String serializeException(Exception e) {
        LogMessage logMessage = new LogMessage(e.getMessage(), LogMessageType.error, e);
        try {
            return objectMapper.writeValueAsString(newArrayList(logMessage));
        } catch (IOException e1) {
            throw new RuntimeException("failed serializing exception", e);
        }
    }
}
