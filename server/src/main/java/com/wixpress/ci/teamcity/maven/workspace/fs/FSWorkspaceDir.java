package com.wixpress.ci.teamcity.maven.workspace.fs;

import com.wixpress.ci.teamcity.maven.workspace.WorkspaceDir;

import java.io.File;

/**
 * @author yoav
 * @since 2/15/12
 */
public class FSWorkspaceDir implements WorkspaceDir{
    private final File dir;

    FSWorkspaceDir(File dir) {
        this.dir = dir;
    }

    FSWorkspaceDir(WorkspaceDir projectDir, String dirName) {
        this.dir = new File(((FSWorkspaceDir)projectDir).dir, dirName);
    }

    public boolean exists() {
        return dir.exists();
    }
    
    File getDir() {
        return dir;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("FSWorkspaceFile(")
                .append(dir.getAbsoluteFile())
                .append(')')
                .toString();
    }

}
