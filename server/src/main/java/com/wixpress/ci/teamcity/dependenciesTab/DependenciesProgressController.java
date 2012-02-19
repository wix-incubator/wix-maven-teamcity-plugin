package com.wixpress.ci.teamcity.dependenciesTab;

import com.wixpress.ci.teamcity.mavenAnalyzer.CollectProgress;
import com.wixpress.ci.teamcity.mavenAnalyzer.TeamCityBuildMavenDependenciesAnalyzer;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * @author yoav
 * @since 2/16/12
 */
public class DependenciesProgressController extends BaseController {
    private final WebControllerManager myManager;
    private final TeamCityBuildMavenDependenciesAnalyzer dependenciesAnalyzer;
    private final ObjectMapper objectMapper;

    public DependenciesProgressController(SBuildServer server, WebControllerManager myManager, TeamCityBuildMavenDependenciesAnalyzer dependenciesAnalyzer, ObjectMapper objectMapper) {
        super(server);
        this.myManager = myManager;
        this.dependenciesAnalyzer = dependenciesAnalyzer;
        this.objectMapper = objectMapper;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        Integer token = Integer.parseInt(request.getParameter("token"));
        CollectProgress collectProgress = dependenciesAnalyzer.getProgress(id, token);
        HashMap params = new HashMap();
        params.put("json", objectMapper.writeValueAsString(collectProgress));
        return new ModelAndView("/plugins/wix-maven-3-teamcity-plugin/renderJson.jsp", params);
    }
    
    public void register() {
        myManager.registerController("/maven-dependencies-plugin/get-dependencies/progress.html", this);
    }
    
}
