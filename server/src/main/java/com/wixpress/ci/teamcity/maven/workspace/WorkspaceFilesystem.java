package com.wixpress.ci.teamcity.maven.workspace;

/**
 * @author yoav
 * @since 2/15/12
 */
public interface WorkspaceFilesystem {
    WorkspaceDir getRoot();

    WorkspaceFile newFile(WorkspaceDir root, String fileName);

    WorkspaceDir newDir(WorkspaceDir projectDir, String module);
}
