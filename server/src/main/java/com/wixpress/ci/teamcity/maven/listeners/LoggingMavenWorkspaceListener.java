package com.wixpress.ci.teamcity.maven.listeners;

import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceListener;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceDir;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceFile;
import org.apache.maven.model.Model;

/**
 * @author yoav
 * @since 2/14/12
 */
public class LoggingMavenWorkspaceListener implements MavenWorkspaceListener {
    
    ListenerLogger out;

    public LoggingMavenWorkspaceListener(ListenerLogger out) {
        this.out = out;
    }

    public void projectRootPomNotFound(WorkspaceFile projectPom) {
        out.error(String.format("Project root pom not found at [%s]", projectPom));
    }

    public void failureReadingPom(WorkspaceFile projectPom, Exception cause){
        out.error(String.format("Failed reading pom file [%s] - %s", projectPom, cause.getMessage()), cause);
    }

    public void failureReadingModulePom(String declaringProject, String module, WorkspaceFile modulePomFile, Exception cause) {
        out.error(String.format("Failed reading module [%s/%s] pom file from [%s] - %s", declaringProject, module, modulePomFile, cause.getMessage()), cause);
    }

    public void moduleDirNotFound(String declaringProject, String module, WorkspaceDir moduleProjectDir) {
        out.error(String.format("Failed finding module [%s/%s] directory at [%s]", declaringProject, module, moduleProjectDir));
    }

    public void modulePomFileNotFound(String declaringProject, String module, WorkspaceDir moduleProjectDir) {
        out.error(String.format("Module [%s/%s] pom file not found at [%s]", declaringProject, module, moduleProjectDir));
    }

    public void loadedRootPomFile(Model model, WorkspaceFile projectPom) {
        out.info(String.format("Loaded root pom file for [%s] from [%s]", model.getId(), projectPom));
    }

    public void loadedModulePomFile(Model model, String module, WorkspaceFile modulePomFile) {
        out.info(String.format("Loaded module [%s] pom file for [%s] from [%s]", module, model.getId(), modulePomFile));
    }
}
