package com.wixpress.ci.teamcity.dependenciesTab;

import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author yoav
 * @since 3/5/12
 */
public class DependenciesAdminTabExtension extends SimpleCustomTab {
    private ConfigModel configModel;

    public DependenciesAdminTabExtension(WebControllerManager manager, ConfigModel configModel) {
        super(manager, PlaceId.ADMIN_SERVER_CONFIGURATION_TAB, "wix-maven-3-teamcity-plugin", "dependenciesAdminTab.jsp", "Maven Dependencies");
        this.configModel = configModel;
        register();
    }

    public void fillModel(@NotNull final Map model, @NotNull final HttpServletRequest request) {
        model.put("commitsToIgnore", toMultilineString());
    }

    private String toMultilineString() {
        StringBuilder sb = new StringBuilder();
        List<String> commitsToIgnore = configModel.getConfig().getCommitsToIgnore();
        for (int i=0; i < commitsToIgnore.size(); i++) {
            sb.append(commitsToIgnore.get(i)).append((i < commitsToIgnore.size()-1)?"\n":"");
        }
            
        return sb.toString();
    }

}
