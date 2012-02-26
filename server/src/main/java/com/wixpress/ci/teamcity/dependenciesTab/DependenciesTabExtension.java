package com.wixpress.ci.teamcity.dependenciesTab;

import com.wixpress.ci.teamcity.DependenciesAnalyzer;
import com.wixpress.ci.teamcity.domain.DependenciesResult;
import com.wixpress.ci.teamcity.domain.LogMessage;
import com.wixpress.ci.teamcity.domain.LogMessageType;
import com.wixpress.ci.teamcity.teamCityAnalyzer.BuildTypesDependencyAnalyzer;
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

    private DependenciesAnalyzer dependenciesAnalyzer;
    private final ObjectMapper objectMapper;

    public DependenciesTabExtension(WebControllerManager manager, ProjectManager projectManager, BuildTypesDependencyAnalyzer buildTypesAnalyzer, ObjectMapper objectMapper) {
        super("wix-maven-3-teamcity-plugin", "Maven 3 Dependencies", manager, projectManager, "dependenciesTab.jsp");
        this.dependenciesAnalyzer = buildTypesAnalyzer;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void fillModel(Map model, HttpServletRequest request, @NotNull SBuildType buildType, SUser user) {
        try {
            DependenciesResult dependenciesResult = dependenciesAnalyzer.analyzeDependencies(buildType);
            model.put("resultType", dependenciesResult.getResultType().name());
            model.put("buildTypeId", buildType.getBuildTypeId());
            switch (dependenciesResult.getResultType()) {
                case current:
                case needsRefresh:
                    model.put("module", objectMapper.writeValueAsString(dependenciesResult.getModule()));
                    model.put("fullTrace", "{}");
                    break;
                case exception:
                    model.put("module", "{}");
                    model.put("fullTrace", objectMapper.writeValueAsString(dependenciesResult.getFullTrace()));
                    break;
                case notRun:
                case runningAsync:
                    model.put("module", "{}");
                    model.put("fullTrace", "{}");
            }
        } catch (Exception e) {
            model.put("module", "{}");
            model.put("fullTrace", serializeException(e));
            model.put("resultType", "error");
            model.put("buildTypeId", buildType.getBuildTypeId());
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
