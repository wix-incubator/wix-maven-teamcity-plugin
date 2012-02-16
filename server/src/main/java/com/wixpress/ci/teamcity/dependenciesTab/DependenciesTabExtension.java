package com.wixpress.ci.teamcity.dependenciesTab;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yoav
 * @since 2/16/12
 */
public class DependenciesTabExtension extends BuildTypeTab {

    private TeamCityProjectDependenciesAnalyzer dependenciesAnalyzer;

    public DependenciesTabExtension(WebControllerManager manager, ProjectManager projectManager, TeamCityProjectDependenciesAnalyzer dependenciesAnalyzer) {
        super("wix-maven-3-teamcity-plugin", "Maven 3 Dependencies", manager, projectManager, "dependenciesTab.jsp");
        this.dependenciesAnalyzer = dependenciesAnalyzer;
    }

    @Override
    protected void fillModel(Map model, HttpServletRequest request, @NotNull SBuildType buildType, SUser user) {
        model.put("dependenciesSession", dependenciesAnalyzer.collectDependencies(buildType));
        model.put("buildName", buildType.getName());
    }
}
