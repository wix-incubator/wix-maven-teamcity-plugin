package com.wixpress.ci.teamcity.maven.workspace;

import org.apache.maven.model.Model;

import java.io.File;

/**
 * @author yoav
 * @since 2/14/12
 */
public interface MavenWorkspaceListener {
    void projectRootPomNotFound(WorkspaceFile projectPom);

    void failureReadingPom(WorkspaceFile projectPom, Exception cause);

    void failureReadingModulePom(String declaringProject, String module, WorkspaceFile modulePomFile, Exception cause);

    void moduleDirNotFound(String declaringProject, String module, WorkspaceDir moduleProjectDir);

    void modulePomFileNotFound(String declaringProject, String module, WorkspaceDir moduleProjectDir);

    void loadedRootPomFile(Model model, WorkspaceFile projectPom);

    void loadedModulePomFile(Model model, String module, WorkspaceFile modulePomFile);
}
