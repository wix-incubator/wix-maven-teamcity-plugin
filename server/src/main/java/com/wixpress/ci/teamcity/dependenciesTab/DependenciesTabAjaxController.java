package com.wixpress.ci.teamcity.dependenciesTab;

import com.wixpress.ci.teamcity.DependenciesAnalyzer;
import com.wixpress.ci.teamcity.mavenAnalyzer.TeamCityBuildMavenDependenciesAnalyzer;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yoav
 * @since 2/16/12
 */
public class DependenciesTabAjaxController extends BaseController {
    private final WebControllerManager myManager;
    private final DependenciesAnalyzer dependenciesAnalyzer;
    private final ProjectManager projectManager;
    private final ObjectMapper objectMapper;
    private final Map<String, Action> actions = new HashMap<String, Action>();

    public DependenciesTabAjaxController(SBuildServer server, WebControllerManager myManager,
                                         final ProjectManager projectManager,
                                         final TeamCityBuildMavenDependenciesAnalyzer dependenciesAnalyzer,
                                         final ObjectMapper objectMapper) {
        super(server);
        this.myManager = myManager;
        this.dependenciesAnalyzer = dependenciesAnalyzer;
        this.objectMapper = objectMapper;
        this.projectManager = projectManager;

        registerProgressOperation(dependenciesAnalyzer);
        registerForceAnalyzeDependencies(projectManager, dependenciesAnalyzer);
        registerGetBuildDependencies(projectManager, dependenciesAnalyzer);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String action = getParameter(request, "action");
        if (actions.containsKey(action))
            return actions.get(action).handle(request, response);
        else {
            writeError(response, 400, String.format("action [%s] not found", action));
            return newEmptyModelAndView();
        }
    }

    public void register() {
        myManager.registerController("/maven-dependencies-plugin.html", this);
    }

    private void registerGetBuildDependencies(final ProjectManager projectManager, final TeamCityBuildMavenDependenciesAnalyzer dependenciesAnalyzer) {
        actions.put("getBuildDependencies", new Action() {
            @Override
            Object doHandle(HttpServletRequest request) throws IOException {
                String id = getParameter(request, "id");
                SBuildType buildType = projectManager.findBuildTypeById(id);
                if (buildType != null)
                    return dependenciesAnalyzer.getBuildDependencies(buildType);
                else
                    throw new ResourceNotFoundException("build [%s] not found", id);
            }
        });
    }

    private void registerForceAnalyzeDependencies(final ProjectManager projectManager, final TeamCityBuildMavenDependenciesAnalyzer dependenciesAnalyzer) {
        actions.put("forceAnalyzeDependencies", new Action() {
            @Override
            Object doHandle(HttpServletRequest request) throws IOException {
                String id = getParameter(request, "id");
                SBuildType buildType = projectManager.findBuildTypeById(id);
                if (buildType != null)
                    return dependenciesAnalyzer.forceAnalyzeDependencies(buildType);
                else
                    throw new ResourceNotFoundException("build [%s] not found", id);
            }
        });
    }

    private void registerProgressOperation(final TeamCityBuildMavenDependenciesAnalyzer dependenciesAnalyzer) {
        actions.put("progress", new Action() {
            @Override
            Object doHandle(HttpServletRequest request) throws IOException {
                String id = getParameter(request, "id");
                Integer token = getParameterAsInt(request, "token");
                return dependenciesAnalyzer.getProgress(id, token);
            }
        });
    }

    private abstract class Action {
        abstract Object doHandle(HttpServletRequest request) throws Exception;

        public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) {
            try {
                Object result = doHandle(request);
                response.setHeader("content-type", "application/json");
                objectMapper.writeValue(response.getOutputStream(), result);
                return newEmptyModelAndView();
            } catch (BadRequestException e) {
                return writeError(response, 400, e);
            } catch (ResourceNotFoundException e) {
                return writeError(response, 404, e);
            } catch (Exception e) {
                return writeError(response, 500, e);
            }
        }

    }

    private ModelAndView writeError(HttpServletResponse response, int code, Exception e) {
        response.setStatus(code);
        try {
            response.getOutputStream().write(e.getMessage().getBytes());
        } catch (IOException e1) {
            // ignore this error
        }
        return newEmptyModelAndView();
    }

    private ModelAndView writeError(HttpServletResponse response, int code, String cause) {
        response.setStatus(code);
        try {
            response.getOutputStream().write(cause.getBytes());
        } catch (IOException e1) {
            // ignore this error
        }
        return newEmptyModelAndView();
    }

    private ModelAndView newEmptyModelAndView() {
       return null;
    }

    protected String getParameter(HttpServletRequest request, String name)  {
        String param = request.getParameter(name);
        if (param == null)
            throw new BadRequestException("param [%s] is missing", name);
        else
            return param;
    }

    protected Integer getParameterAsInt(HttpServletRequest request, String name) {
        try {
            return Integer.parseInt(getParameter(request, name));
        }
        catch (NumberFormatException e) { 
            throw new BadRequestException("param [%s] is not a number", name);
        }
    }

    private class ResourceNotFoundException extends RuntimeException {

        public ResourceNotFoundException(String message, Object... args) {
            super(String.format(message, args));
        }
    }

    private class BadRequestException extends RuntimeException {

        public BadRequestException(String message, Object... args) {
            super(String.format(message, args));
        }
    }
}
