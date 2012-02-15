package com.wixpress.ci.teamcity.maven.workspace.fs;

import com.wixpress.ci.teamcity.maven.workspace.WorkspaceDir;
import com.wixpress.ci.teamcity.maven.workspace.WorkspaceFile;

import java.io.File;

/**
 * @author yoav
 * @since 2/15/12
 */
public class FSWorkspaceFile implements WorkspaceFile {
    private final File file;
    private FSWorkspaceDir parent;

    FSWorkspaceFile(WorkspaceDir dir, String fileName) {
        file = new File(((FSWorkspaceDir)dir).getDir(), fileName);
        parent = (FSWorkspaceDir)dir;
    }

    public boolean exists() {
        return file.exists();
    }

    public File getFile() {
        return file;
    }

    public WorkspaceDir getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("WorkspaceFile(")
                .append(file.getAbsoluteFile())
                .append(')')
                .toString();
    }

    public String getRelativePath() {
        return parent.getRelativePath().append(file.getName()).toString();
    }
}
