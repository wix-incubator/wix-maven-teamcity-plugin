package com.wixpress.ci.teamcity.maven.workspace.fs;

import com.wixpress.ci.teamcity.maven.workspace.WorkspaceDir;

import java.io.File;

/**
 * @author yoav
 * @since 2/15/12
 */
public class FSWorkspaceDir implements WorkspaceDir{
    private final File dir;
    private final FSWorkspaceDir parent;

    FSWorkspaceDir(File dir) {
        this.dir = dir;
        parent = null;
    }

    FSWorkspaceDir(WorkspaceDir parent, String dirName) {
        this.dir = new File(((FSWorkspaceDir)parent).dir, dirName);
        this.parent = (FSWorkspaceDir)parent;
    }

    public boolean exists() {
        return dir.exists();
    }

    public WorkspaceDir getParent() {
        return parent;
    }

    File getDir() {
        return dir;
    }
    
    StringBuilder getRelativePath() {
        if (parent != null)
            return parent.getRelativePath().append(dir.getName()).append("/");
        else
            return new StringBuilder();
    }
    

    @Override
    public String toString() {
        return new StringBuilder()
                .append("WorkspaceDir(")
                .append(dir.getAbsoluteFile())
                .append(')')
                .toString();
    }

}
