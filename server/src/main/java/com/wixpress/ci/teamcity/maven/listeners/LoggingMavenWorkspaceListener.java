package com.wixpress.ci.teamcity.maven.listeners;

import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceListener;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceDir;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceFile;
import org.apache.maven.model.Model;

import java.io.File;
import java.io.PrintStream;

/**
 * @author yoav
 * @since 2/14/12
 */
public class LoggingMavenWorkspaceListener implements MavenWorkspaceListener {
    
    PrintStream out;

    public LoggingMavenWorkspaceListener(PrintStream out) {
        this.out = out;
    }

    public LoggingMavenWorkspaceListener() {
        this(System.out);
    }

    public void projectRootPomNotFound(WorkspaceFile projectPom) {
        out.printf("Project root pom not found at [%s]\n", projectPom);
    }

    public void failureReadingPom(WorkspaceFile projectPom, Exception cause){
        out.printf("Failed reading pom file [%s] - %s\n", projectPom, cause.getMessage());
    }

    public void failureReadingModulePom(String declaringProject, String module, WorkspaceFile modulePomFile, Exception cause) {
        out.printf("Failed reading module [%s/%s] pom file from [%s] - %s\n", declaringProject, module, modulePomFile, cause.getMessage());
    }

    public void moduleDirNotFound(String declaringProject, String module, WorkspaceDir moduleProjectDir) {
        out.printf("Failed finding module [%s/%s] directory at [%s]\n", declaringProject, module, moduleProjectDir);
    }

    public void modulePomFileNotFound(String declaringProject, String module, WorkspaceDir moduleProjectDir) {
        out.printf("Module [%s/%s] pom file not found at [%s] \n", declaringProject, module, moduleProjectDir);
    }

    public void loadedRootPomFile(Model model, WorkspaceFile projectPom) {
        out.printf("Loaded root pom file for [%s] from [%s]\n", model.getId(), projectPom);
    }

    public void loadedModulePomFile(Model model, String module, WorkspaceFile modulePomFile) {
        out.printf("Loaded module [%s] pom file for [%s] from [%s]\n", module, model.getId(), modulePomFile);
    }
}
