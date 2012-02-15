package com.wixpress.ci.teamcity.maven.workspace.fs;

import com.wixpress.ci.teamcity.maven.workspace.WorkspaceDir;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceFile;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceFilesystem;

import java.io.File;

/**
 * @author yoav
 * @since 2/15/12
 */
public class FSWorkspaceFilesystem implements WorkspaceFilesystem {
    
    private File rootDir;
    
    public FSWorkspaceFilesystem(File rootDir) {
        this.rootDir = rootDir;
    }
    
    public WorkspaceDir getRoot() {
        return new FSWorkspaceDir(rootDir);
    }

    public WorkspaceFile newFile(WorkspaceDir root, String fileName) {
        return new FSWorkspaceFile(root, fileName);
    }

    public WorkspaceDir newDir(WorkspaceDir projectDir, String dirName) {
        return new FSWorkspaceDir(projectDir, dirName);
    }
}
